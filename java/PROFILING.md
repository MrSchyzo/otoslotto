# Profiling

`./scripts/profile.sh` runs `MyBenchmark` (which exercises
`VectorisedTransposedBitmaskLottery.getWinnersHistogram()`) with GC, JIT, and
allocation instrumentation enabled, and writes everything to a timestamped
directory under `profiling-results/`.

```
./scripts/profile.sh
```

Requires the project's JDK (26, Temurin) on `PATH`, plus `jps` (bundled with
the JDK) and [async-profiler](https://github.com/async-profiler/async-profiler)'s
`asprof` CLI for the allocation histogram (`brew install async-profiler` on
macOS). Without `asprof`/`jps`, the script still runs — you just lose the
allocation-by-class breakdown.

Each run produces:

| File | What it's for |
|---|---|
| `allocation-by-class.txt` | Which classes get allocated |
| `alloc-flamegraph.html` | Same data, browsable flamegraph |
| `run.log` | GC allocation-rate numbers + JIT compilation/intrinsics log (interleaved with JMH's own iteration output) |
| `jmh-results.json` | Raw JMH throughput/latency numbers |
| `jfr/.../profile.jfr` | General JFR recording (GC pauses, compilation events, etc.) |

## Which classes are allocated

Source: **async-profiler's `alloc` engine**, attached directly to the forked
benchmark JVM while it runs (see `allocation-by-class.txt` /
`alloc-flamegraph.html`).

A run on this machine (Apple M1 Pro, arm64) showed, among others:

```
       246 samples  java.util.HashMap$Node
        13 samples  jdk.incubator.vector.Long128Vector
         6 samples  long[]
         5 samples  jdk.incubator.vector.Long128Vector
```

Two things worth knowing when reading this:
- `HashMap$Node` comes from `MyBenchmark.setUp()`'s `HashSet`-based sampling
  of the 10M picks — it's setup noise, not the hot path being measured.
  Check the flamegraph's call stack to tell setup allocations apart from
  `getWinnersHistogram` ones.
- `Long128Vector` is the *concrete* implementation `LongVector.SPECIES_PREFERRED`
  resolves to on this hardware — 128-bit vectors, i.e. 2 lanes of `long` per
  vector, matching ARM NEON's register width (an x86 box with AVX2/AVX-512
  would resolve to a wider species and a different concrete class here). Its
  appearance in the allocation profile means some vector values do escape to
  the heap — a useful, sometimes surprising counterpoint to the assumption
  that Vector API code is allocation-free.

## How fast is the allocation

Source: **JMH's built-in GC profiler** (`-prof gc:churn=true`). Grep
`run.log` for `gc.alloc.rate`:

```
Iteration   2: 500261 ...
                 gc.alloc.rate:      0,184 MB/sec
                 gc.alloc.rate.norm: 107,072 B/op
                 gc.churn.G1_Eden_Space.norm: 20053,111 B/op
```

`gc.alloc.rate` is MB/sec of total allocation during that iteration;
`gc.alloc.rate.norm` is bytes allocated per **op**. Caveat: this benchmark
uses `@BenchmarkMode(Mode.SampleTime)`, which times *batches* of invocations,
not each individual call — so `gc.alloc.rate.norm` is bytes-per-batch, not
bytes-per-call. Don't read ~100KB/op as "one call to `getWinnersHistogram`
allocates 100KB"; the batch size divides that back down to a much smaller
per-call figure. The first iteration's numbers are usually much higher than
the rest (JIT still warming up, deopts) — trust the later, steady-state
iterations.

## JIT / SIMD evidence (in place of raw assembly)

No `-XX:+PrintAssembly` / hsdis / `-prof perfasm` here: this repo runs on
**macOS + arm64 + a very recent JDK (26)**, `-prof perfasm` depends on Linux
`perf` and doesn't work on macOS at all, and no prebuilt hsdis disassembler
plugin exists yet for this exact JDK/arch combination. Instead, `run.log`
carries `-XX:+PrintCompilation -XX:+PrintInlining -XX:+PrintIntrinsics`
output, which shows the mechanism that *produces* the SIMD instructions even
without printing them as text.

The evidence to look for — grep `run.log` for `getWinnersHistogram` and for
`intrinsic`:

```
@ 33   jdk.internal.vm.vector.VectorSupport::load (38 bytes)   (intrinsic)   late inline succeeded
@ 155  jdk.internal.vm.vector.VectorSupport::binaryOp (38 bytes)  (intrinsic)   late inline succeeded
@ 19   jdk.internal.vm.vector.VectorSupport::fromBitsCoerced (35 bytes)   (intrinsic)   late inline succeeded
```

`VectorSupport::load`/`binaryOp`/`fromBitsCoerced`/`extract` are the JIT
compiler's intrinsic hooks for the Vector API — `(intrinsic)` + `late inline
succeeded` means C2 recognized the call and replaced it with a hand-written
IR node that lowers directly to native vector instructions (NEON, on this
hardware), instead of compiling `LongVector.lanewise(...)` as a regular
method call. This is the actual mechanism that makes the code SIMD; if these
lines were missing or said "failed to inline", that would mean the JIT fell
back to scalar code.

You'll also see `getWinnersHistogram` recompiled multiple times over a run
(tier 3 → tier 4, OSR entries marked `%`, occasional `made not entrant:
uncommon trap` / `OSR invalidation of lower level`) — this is normal HotSpot
tiered-compilation churn as it collects more profiling data and promotes the
method to more aggressive optimization, not a sign of a problem.

If you do want literal disassembly later: try a prebuilt hsdis from
[builds.shipilev.net/hsdis](https://builds.shipilev.net/hsdis/) for
`aarch64`/this JDK version first; if none exists, it needs to be built from
the matching [openjdk/jdk](https://github.com/openjdk/jdk) source tag with
`make build-hsdis` (capstone backend, via `brew install capstone`), then
pointed at with `-XX:+PrintAssembly -XX:CompileCommand=compileonly,*VectorisedTransposedBitmaskLottery.getWinnersHistogram`.
`-prof perfasm` will still not work on macOS regardless (no `perf`).

## Known caveat: JFR's `jdk.ObjectAllocationSample` is empty here

The `-prof jfr` recording under `jfr/` almost always shows **zero**
`jdk.ObjectAllocationSample` / `ObjectAllocationInNewTLAB` /
`ObjectAllocationOutsideTLAB` events for this benchmark (confirmed
reproducible, including in earlier manual `jcmd JFR.start`/`JFR.dump`
recordings of this same benchmark). This isn't a bug in the script — it was
verified against a synthetic high-allocation program on the same JDK, where
the same JFR mechanism fires thousands of samples per recording. The
difference is that this benchmark's real, steady-state allocation rate is
genuinely low (well under 1 MB/sec, per the GC profiler numbers above), so
TLABs refill rarely enough that JFR's probabilistic allocation sampler
essentially never catches one in a ~1 minute recording window. That's why
this script uses async-profiler's `alloc` engine (not throttled the same
way) for the class-histogram instead — treat the JFR recording as useful for
GC/compilation events, not allocation class histograms, for this particular
benchmark.

## Comparing implementations

`MyBenchmark.java` benchmarks `VectorisedTransposedBitmaskLottery` by
default; swap the commented-out line to point at `BitmaskLottery` or
`TransposedBitmaskLottery` and rerun `./scripts/profile.sh` to compare the
scalar and vectorized implementations' allocation and JIT behavior
side-by-side.
