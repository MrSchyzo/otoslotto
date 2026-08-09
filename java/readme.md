# Hungarian Ötöslottó

Hungarian lottery where 5 distinct numbers in `{1..90}` are extracted.

The program is a CLI application which loads all plays written in `./players.txt`. Then, it asks for a winning draw, and
it returns the count of the 2,3,4,5-match winners in histogram fashion.

## Requirements

Just Java 17.

## Used tech
1. `Java 26` for the runtime
2. `gradle 9.7.0` for the build management
3. `JMH` and `JUnit` for testing and benchmarking
4. JVM argument `--add-modules=jdk.incubator.vector` for the VectorAPI (without this, the app will fail)

## How to
1. Build the app `./gradlew build`
2. Test the app `./gradlew test`
3. Benchmark the app `./gradlew jmh`

### After app is built
1. Generate a sample 10M file in the current working directory: `./gradlew run`
2. Run the application: `./gradlew run --args="exec"`
