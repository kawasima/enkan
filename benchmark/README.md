# Enkan vs Spring Boot Benchmark

HTTP throughput and latency comparison between Enkan (Undertow) and Spring Boot (Tomcat) using [Vegeta](https://github.com/tsenart/vegeta).

## Prerequisites

- Java 21+
- Maven 3.9+
- [Vegeta](https://github.com/tsenart/vegeta) (`brew install vegeta`)

## Quick Start

```sh
cd benchmark
chmod +x run-benchmark.sh
./run-benchmark.sh
```

## Configuration

Environment variables:

| Variable   | Default | Description            |
|------------|---------|------------------------|
| `RATE`     | 1000    | Requests per second    |
| `DURATION` | 30s     | Duration per scenario  |

Example: `RATE=5000 DURATION=60s ./run-benchmark.sh`

## Scenarios

### 1. plaintext (`GET /hello`)

Returns `"Hello, World!"` as `text/plain`. Measures base framework overhead with minimal processing.

### 2. json (`GET /json`)

Returns `{"message":"Hello, World!","timestamp":...}` as `application/json`. Adds JSON serialization overhead.

### 3. params+session (`GET /echo?name=world`)

Parses query parameters, reads/writes an in-memory session with a counter, returns JSON. Exercises the full middleware stack: param parsing, cookies, session management, and response serialization.

## Middleware Stack Equivalence

Both applications register equivalent processing layers:

| Enkan                        | Spring Boot                              |
|------------------------------|------------------------------------------|
| DefaultCharsetMiddleware     | CharacterEncodingFilter (auto-configured)|
| SecurityHeadersMiddleware    | Custom SecurityHeadersFilter             |
| ContentTypeMiddleware        | Spring content-type handling             |
| ParamsMiddleware             | Servlet query param parsing              |
| NestedParamsMiddleware       | (no direct equivalent)                   |
| CookiesMiddleware            | Servlet cookie handling                  |
| SessionMiddleware (Memory)   | HttpSession (in-memory Tomcat)           |
| ContentNegotiationMiddleware | Spring ContentNegotiationManager         |
| RoutingMiddleware            | @RestController + @GetMapping            |
| SerDesMiddleware (Jackson)   | Jackson HttpMessageConverter             |
| ControllerInvokerMiddleware  | Spring MVC method invocation             |

## Caveats

- The first 5 seconds of each run serve as JVM warmup (separate warmup phase at lower rate precedes the actual benchmark).
- Undertow and Tomcat have different default thread pool sizes. For production-grade comparison, tune both to the same thread count.
- Results include the full network round-trip on localhost. For micro-benchmarking specific layers, use JMH instead.

## Results

Raw Vegeta binary files are saved to `results/`. To re-analyze:

```sh
vegeta report < results/enkan-json.bin
vegeta report -type=hist[0,1ms,5ms,10ms,25ms,50ms] < results/enkan-json.bin
vegeta plot < results/enkan-json.bin > results/enkan-json.html
```
