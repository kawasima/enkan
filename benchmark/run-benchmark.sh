#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"
mkdir -p "${RESULTS_DIR}"

ENKAN_PORT=8080
SPRING_PORT=8081
RATE=${RATE:-10000}
DURATION=${DURATION:-15s}
WARMUP_RATE=100
WARMUP_DURATION=5s
ENKAN_PID=""
SPRING_PID=""

cleanup() {
    echo "Shutting down applications..."
    [[ -n "${ENKAN_PID}" ]] && kill "${ENKAN_PID}" 2>/dev/null || true
    [[ -n "${SPRING_PID}" ]] && kill "${SPRING_PID}" 2>/dev/null || true
    wait "${ENKAN_PID}" 2>/dev/null || true
    wait "${SPRING_PID}" 2>/dev/null || true
    echo "Done."
}
trap cleanup EXIT

wait_for_port() {
    local port=$1
    local name=$2
    local attempts=0
    echo "Waiting for ${name} on port ${port}..."
    until curl -sf "http://localhost:${port}/hello" > /dev/null 2>&1; do
        attempts=$((attempts + 1))
        if [[ ${attempts} -gt 60 ]]; then
            echo "ERROR: ${name} did not start within 60 seconds"
            exit 1
        fi
        sleep 1
    done
    echo "${name} is ready."
}

# ─── Step 1: Install Enkan modules to local Maven repo ───
echo "=== Building Enkan modules (mvn install) ==="
cd "${ROOT_DIR}"
mvn install -pl enkan-core,enkan-system,enkan-web,enkan-servlet,enkan-component-undertow,enkan-component-jackson,kotowari -am -DskipTests -q

# ─── Step 2: Build enkan-app fat JAR ───
echo "=== Building enkan-app ==="
cd "${SCRIPT_DIR}/enkan-app"
mvn package -DskipTests -q

# ─── Step 3: Build spring-app fat JAR ───
echo "=== Building spring-app ==="
cd "${SCRIPT_DIR}/spring-app"
mvn package -DskipTests -q

# ─── Step 4: Start both applications ───
echo "=== Starting Enkan on port ${ENKAN_PORT} ==="
java -jar "${SCRIPT_DIR}/enkan-app/target/enkan-benchmark-app-1.0-SNAPSHOT.jar" \
    > "${RESULTS_DIR}/enkan-app.log" 2>&1 &
ENKAN_PID=$!

echo "=== Starting Spring Boot on port ${SPRING_PORT} ==="
java -jar "${SCRIPT_DIR}/spring-app/target/spring-benchmark-app-1.0-SNAPSHOT.jar" \
    > "${RESULTS_DIR}/spring-app.log" 2>&1 &
SPRING_PID=$!

wait_for_port "${ENKAN_PORT}" "Enkan"
wait_for_port "${SPRING_PORT}" "Spring Boot"

echo ""
echo "================================================================"
echo " Both applications are ready."
echo " Rate: ${RATE} req/s, Duration: ${DURATION}"
echo "================================================================"

# ─── Warmup ───
echo ""
echo "=== Warming up JVMs (${WARMUP_DURATION} at ${WARMUP_RATE} req/s) ==="
echo "GET http://localhost:${ENKAN_PORT}/json" | vegeta attack -rate="${WARMUP_RATE}" -duration="${WARMUP_DURATION}" > /dev/null
echo "GET http://localhost:${SPRING_PORT}/json" | vegeta attack -rate="${WARMUP_RATE}" -duration="${WARMUP_DURATION}" > /dev/null
echo "Warmup complete."

# ─── Benchmark helper ───
run_vegeta() {
    local label=$1
    local url=$2
    local tag=$3

    echo "  [${label}] ${url}"
    echo "GET ${url}" \
        | vegeta attack -rate="${RATE}" -duration="${DURATION}" \
        | tee "${RESULTS_DIR}/${tag}.bin" \
        | vegeta report
    echo ""
}

# ─── Scenario 1: Plain text ───
echo ""
echo "--- Scenario: plaintext ---"
run_vegeta "Enkan"       "http://localhost:${ENKAN_PORT}/hello"  "enkan-plaintext"
run_vegeta "Spring Boot" "http://localhost:${SPRING_PORT}/hello" "spring-plaintext"

# ─── Scenario 2: JSON ───
echo "--- Scenario: json ---"
run_vegeta "Enkan"       "http://localhost:${ENKAN_PORT}/json"  "enkan-json"
run_vegeta "Spring Boot" "http://localhost:${SPRING_PORT}/json" "spring-json"

# ─── Scenario 3: Params + Session ───
echo "--- Scenario: params+session ---"
run_vegeta "Enkan"       "http://localhost:${ENKAN_PORT}/echo?name=world"  "enkan-params-session"
run_vegeta "Spring Boot" "http://localhost:${SPRING_PORT}/echo?name=world" "spring-params-session"

echo ""
echo "================================================================"
echo " Benchmark complete. Raw results in: ${RESULTS_DIR}/"
echo "================================================================"
