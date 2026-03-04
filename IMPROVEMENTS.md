# Enkan Improvements

This document tracks proposed improvements and missing features identified through code review and modern web development gap analysis.

## Security Headers

### IMP-001: HstsMiddleware (HIGH)

Add `Strict-Transport-Security` response header middleware.

**Rationale**: HTTPS enforcement is a baseline security requirement for production applications. Currently only `ForwardedSchemeMiddleware` exists for scheme detection, but there is no HSTS header support.

**Proposed API**:
```java
app.use(new HstsMiddleware());
// Default: max-age=31536000; includeSubDomains
```

**Configurable properties**:
- `maxAge` (long, seconds) — default `31536000` (1 year)
- `includeSubDomains` (boolean) — default `true`
- `preload` (boolean) — default `false`

---

### IMP-002: ContentSecurityPolicyMiddleware (HIGH)

Add `Content-Security-Policy` response header middleware.

**Rationale**: CSP is the primary defense against XSS in modern browsers. The existing `XssProtectionMiddleware` sets `X-XSS-Protection` which is deprecated and ignored by modern browsers.

**Proposed API**:
```java
ContentSecurityPolicyMiddleware csp = new ContentSecurityPolicyMiddleware();
csp.setPolicy("default-src 'self'; script-src 'self' 'nonce-{nonce}'");
app.use(csp);
```

**Configurable properties**:
- `policy` (String) — raw CSP directive string
- `reportOnly` (boolean) — use `Content-Security-Policy-Report-Only` instead

---

### IMP-003: ReferrerPolicyMiddleware (MEDIUM)

Add `Referrer-Policy` response header middleware.

**Rationale**: Controls how much referrer information is included with requests. Required for privacy compliance (GDPR etc.).

**Proposed API**:
```java
ReferrerPolicyMiddleware rp = new ReferrerPolicyMiddleware();
rp.setPolicy("strict-origin-when-cross-origin"); // default
app.use(rp);
```

**Valid values**: `no-referrer`, `no-referrer-when-downgrade`, `origin`, `origin-when-cross-origin`, `same-origin`, `strict-origin`, `strict-origin-when-cross-origin`, `unsafe-url`

---

### IMP-004: PermissionsPolicyMiddleware (LOW)

Add `Permissions-Policy` response header middleware.

**Rationale**: Controls access to browser APIs (camera, microphone, geolocation, etc.).

**Proposed API**:
```java
PermissionsPolicyMiddleware pp = new PermissionsPolicyMiddleware();
pp.setPolicy("camera=(), microphone=(), geolocation=()");
app.use(pp);
```

---

## Operational / Cloud-Native

### IMP-005: Health Check Endpoint (HIGH)

Add a built-in health check endpoint that reflects `EnkanSystem` component status.

**Rationale**: Essential for Kubernetes liveness/readiness probes and load balancer health checks. No equivalent exists in the framework.

**Implemented API**:
```java
// Register system name in EnkanSystem.getComponentMap()
// Components that want custom health reporting implement HealthCheckable:
public class MyComponent extends SystemComponent<MyComponent>
        implements HealthCheckable {
    @Override
    public HealthStatus health() {
        return isConnected() ? HealthStatus.UP : HealthStatus.DOWN;
    }
}

// Add to application routing:
app.use(path("^/health$"), new HealthEndpoint(system));
```

**Response format** (JSON):
```json
{ "status": "UP", "components": { "datasource": "UP", "cache": "UP" } }
```

**HTTP status codes**:

- `200 OK` — all HealthCheckable components UP (or none implement it)
- `503 Service Unavailable` — one or more components DOWN

**Implementation**:

- `enkan-system`: `HealthStatus` enum (UP/DOWN), `HealthCheckable` interface
- `enkan-web`: `HealthEndpoint` — queries `EnkanSystem.getComponentMap()`, invokes `health()` on each `HealthCheckable`, builds JSON manually (no Jackson dependency)

---

### IMP-006: Response Compression Middleware (HIGH)

Add `gzip` (and optionally `brotli`) response compression middleware.

**Rationale**: Both Jetty and Undertow support compression natively but Enkan provides no configuration interface. Responses without compression are unnecessarily large.

**Proposed API**:
```java
CompressMiddleware compress = new CompressMiddleware();
compress.setMinLength(1024); // only compress responses >= 1KB
compress.setMimeTypes(Set.of("text/html", "application/json", "text/css"));
app.use(compress);
```

**Configurable properties**:
- `minLength` (int, bytes) — minimum response size to compress; default `1024`
- `mimeTypes` (Set<String>) — MIME types to compress
- `level` (int) — gzip compression level 1–9; default `6`

---

### IMP-007: CacheControlMiddleware (MEDIUM)

Add `Cache-Control` and `ETag` response header middleware.

**Rationale**: No cache control headers are set by default, causing browsers to re-fetch all resources. Static assets and immutable API responses should be cacheable.

**Proposed API**:
```java
CacheControlMiddleware cache = new CacheControlMiddleware();
cache.setStaticPattern(Pattern.compile("^/assets/"));
cache.setStaticMaxAge(Duration.ofDays(365));
cache.setDynamicDirective("no-cache");
app.use(cache);
```

---

## Authentication / Authorization

### IMP-008: JWT Auth Backend (MEDIUM)

Add a built-in `AuthBackend` implementation that validates JWT tokens from the `Authorization: Bearer` header.

**Rationale**: JWT is the de facto standard for stateless API authentication. The `TokenBackend` interface exists but has no implementation.

**Proposed API**:
```java
JwtAuthBackend jwt = new JwtAuthBackend();
jwt.setSecretKey("my-secret");
jwt.setAlgorithm("HS256");
app.use(new AuthenticationMiddleware(List.of(jwt)));
```

**Dependency**: Would require adding `nimbus-jose-jwt` or `java-jwt` to `enkan-web` or a new `enkan-auth-jwt` module.

---

## Observability

### IMP-009: OpenTelemetry Tracing Integration (MEDIUM)

Add a middleware that propagates OpenTelemetry trace context across HTTP requests.

**Rationale**: `enkan-component-metrics` uses Dropwizard Metrics (JMX-only). Modern observability stacks (Jaeger, Zipkin, Datadog) require OpenTelemetry-compatible trace propagation. Especially important in microservices.

**Proposed API**:
```java
// As a new module: enkan-component-opentelemetry
TracingMiddleware tracing = new TracingMiddleware(openTelemetry);
app.use(tracing);
```

**Behavior**:
- Extracts `traceparent`/`tracestate` headers from incoming requests (W3C Trace Context)
- Creates spans per request with method, path, status attributes
- Propagates context to outgoing requests via `OkHttp`/`HttpClient` instrumentation

---

### IMP-010: Structured Logging Support (LOW)

Add a request logging middleware that outputs structured JSON logs.

**Rationale**: Current logging uses plain text via SLF4J. Log aggregation tools (ELK, Datadog, Splunk) work best with structured JSON including request ID, method, path, status, duration.

**Proposed API**:
```java
AccessLogMiddleware log = new AccessLogMiddleware();
log.setFormat(AccessLogFormat.JSON); // or COMBINED (Apache combined format)
app.use(log);
```

**JSON log fields**: `timestamp`, `requestId`, `method`, `uri`, `status`, `durationMs`, `remoteAddr`, `userAgent`

---

## Legend

| Priority | Description |
|----------|-------------|
| HIGH     | Commonly required in production applications; significant gap |
| MEDIUM   | Frequently needed; workaround exists but is cumbersome |
| LOW      | Nice to have; narrow use cases |

## Status

| ID | Title | Priority | Status |
|----|-------|----------|--------|
| IMP-001 | HstsMiddleware | HIGH | Done (via SecurityHeadersMiddleware) |
| IMP-002 | ContentSecurityPolicyMiddleware | HIGH | Done (via SecurityHeadersMiddleware) |
| IMP-003 | ReferrerPolicyMiddleware | MEDIUM | Done (via SecurityHeadersMiddleware) |
| IMP-004 | PermissionsPolicyMiddleware | LOW | Proposed |
| IMP-005 | Health Check Endpoint | HIGH | Done (HealthCheckable + HealthEndpoint) |
| IMP-006 | Response Compression Middleware | HIGH | Done (JettyAdapter: CompressionHandler+GzipCompression, UndertowAdapter: EncodingHandler) |
| IMP-007 | CacheControlMiddleware | MEDIUM | Proposed |
| IMP-008 | JWT Auth Backend | MEDIUM | Proposed |
| IMP-009 | OpenTelemetry Tracing Integration | MEDIUM | Proposed |
| IMP-010 | Structured Logging Support | LOW | Proposed |
