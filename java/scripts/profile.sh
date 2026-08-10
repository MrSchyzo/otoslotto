#!/usr/bin/env bash
set -uo pipefail

# Runs MyBenchmark with:
#   - JMH's GC profiler          -> allocation rate/speed
#   - async-profiler (alloc)     -> which classes get allocated, attached to the
#                                    forked benchmark JVM while it runs
#   - JMH's JFR profiler         -> general .jfr recording (GC, compilation, etc.)
#   - JIT diagnostic flags       -> proof the Vector API calls get intrinsified as SIMD
# See ../PROFILING.md for how to read the output.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$JAVA_DIR"

JAVA_BIN="$(command -v java)"
JAVA_BIN_DIR="$(dirname "$JAVA_BIN")"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
OUT_DIR="profiling-results/$TIMESTAMP"
mkdir -p "$OUT_DIR/jfr"

echo "==> Building JMH benchmark jar"
./gradlew jmhJar

JMH_JAR="$(ls build/libs/*-jmh.jar | head -n1)"
echo "==> Using benchmark jar: $JMH_JAR"
echo "==> Using JDK: $("$JAVA_BIN" -version 2>&1 | head -n1)"

echo "==> Running MyBenchmark (GC profiler + JFR + JIT diagnostics)"
echo "    Output: $OUT_DIR"

"$JAVA_BIN" \
  --add-modules=jdk.incubator.vector \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:+DebugNonSafepoints \
  -XX:+PrintCompilation \
  -XX:+PrintInlining \
  -XX:+PrintIntrinsics \
  -jar "$JMH_JAR" \
  -prof gc:churn=true \
  -prof "jfr:dir=$OUT_DIR/jfr" \
  -rf json -rff "$OUT_DIR/jmh-results.json" \
  MyBenchmark \
  2>&1 | tee "$OUT_DIR/run.log" &
JMH_PID=$!

ASPROF_BIN="$(command -v asprof || true)"
JPS_BIN="$JAVA_BIN_DIR/jps"

if [ -z "$ASPROF_BIN" ] || [ ! -x "$JPS_BIN" ]; then
  echo "==> Skipping async-profiler allocation capture (asprof and/or jps not found)"
  wait "$JMH_PID"
else
  echo "==> Waiting for forked benchmark JVM to start"
  CHILD_PID=""
  for _ in $(seq 1 60); do
    CHILD_PID="$("$JPS_BIN" -l 2>/dev/null | awk '/ForkedMain/ {print $1}' | head -n1)"
    [ -n "$CHILD_PID" ] && break
    sleep 1
  done

  if [ -z "$CHILD_PID" ]; then
    echo "    WARNING: could not find forked benchmark JVM; skipping async-profiler capture"
    wait "$JMH_PID"
  else
    echo "==> Attaching async-profiler (alloc) to forked benchmark JVM (pid $CHILD_PID)"
    "$ASPROF_BIN" start -e alloc "$CHILD_PID"

    # Periodically dump (without stopping) so we still have data even though the
    # forked JVM can exit before we get a chance to react to it dying.
    while kill -0 "$CHILD_PID" 2>/dev/null; do
      "$ASPROF_BIN" dump -o collapsed -f "$OUT_DIR/alloc.collapsed" "$CHILD_PID" >/dev/null 2>&1 || true
      "$ASPROF_BIN" dump -o flamegraph -f "$OUT_DIR/alloc-flamegraph.html" "$CHILD_PID" >/dev/null 2>&1 || true
      sleep 3
    done

    wait "$JMH_PID"

    if [ -s "$OUT_DIR/alloc.collapsed" ]; then
      echo "==> Aggregating allocation-by-class histogram from async-profiler data"
      awk -F'[; ]' '
        {
          leaf = $(NF - 1)
          gsub(/_\[i\]$/, "", leaf)
          count[leaf] += $NF
        }
        END {
          for (c in count) printf "%10d samples  %s\n", count[c], c
        }
      ' "$OUT_DIR/alloc.collapsed" | sort -rn > "$OUT_DIR/allocation-by-class.txt"
      echo "    Allocation-by-class histogram: $OUT_DIR/allocation-by-class.txt"
      echo "    Allocation flamegraph (open in a browser): $OUT_DIR/alloc-flamegraph.html"
    else
      echo "    WARNING: no allocation samples captured by async-profiler"
    fi
  fi
fi

cat <<EOF

==> Done. Results in $OUT_DIR:
  - allocation-by-class.txt   which classes get allocated (async-profiler alloc samples)
  - alloc-flamegraph.html     same data, browsable flamegraph
  - run.log                   grep 'gc.alloc.rate' for allocation speed; grep -E 'intrinsic|Compiled' for JIT/SIMD evidence
  - jmh-results.json          raw JMH throughput/latency numbers
  - jfr/                      raw .jfr recording (open with 'jfr print' or JDK Mission Control) -- note:
                              jdk.ObjectAllocationSample is typically near-empty here, see PROFILING.md

See PROFILING.md for how to interpret each of these.
EOF
