#!/bin/bash

# URL to test
URL="https://site1.local:8443/"

# Number of threads
THREADS=12

# Number of connections
CONNECTIONS=400

# Duration of the test
DURATION="30s"

# Output file
OUTPUT_FILE="wrk_benchmark.txt"

echo "Running wrk benchmark..."
wrk -t$THREADS -c$CONNECTIONS -d$DURATION $URL | tee $OUTPUT_FILE

echo "Benchmark completed. Results saved in $OUTPUT_FILE."
