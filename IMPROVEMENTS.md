# Enkan Improvements

This document tracks proposed improvements and missing features identified through code review and modern web development gap analysis.

## Security Headers

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

## Java 21 Modernization

### IMP-012: Unify Switch Expressions / Pattern Switch (HIGH)

Replace old `case LABEL: ... break;` style with arrow-style switch expressions. Convert instanceof if-else chains to switch pattern matching.

**Affected files**:

- `JShellRepl` — `event.status()` switch
- `TransactionMiddleware` — `TxType` switch
- `MultipartParser` — state machine switch
- `CodecUtils` — char switch
- `StacktraceMiddleware` — exception type if-else chain to switch pattern matching

---

### IMP-013: Introduce Sealed Classes (MEDIUM)

Seal the exception hierarchy to enable exhaustiveness checks in switch expressions.

**Target**:

- `UnrecoverableException` hierarchy:

  ```java
  public sealed abstract class UnrecoverableException extends RuntimeException
      permits MisconfigurationException, FalteringEnvironmentException, UnreachableException {}
  ```

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
| IMP-004 | PermissionsPolicyMiddleware | LOW | Proposed |
| IMP-007 | CacheControlMiddleware | MEDIUM | Done |
| IMP-008 | JWT Auth Backend | MEDIUM | Proposed |
| IMP-009 | OpenTelemetry Tracing Integration | MEDIUM | Done |
| IMP-010 | Structured Logging Support | LOW | Proposed |
| IMP-011 | Unify Pattern Matching instanceof | HIGH | Done |
| IMP-012 | Unify Switch Expressions / Pattern Switch | HIGH | Proposed |
| IMP-013 | Introduce Sealed Classes | MEDIUM | Proposed |
| IMP-014 | Convert to Records | MEDIUM | Done |
| IMP-016 | Unify `Stream.toList()` | LOW | Done |
| IMP-017 | findAny → findFirst in ControllerInvokerMiddleware | HIGH | Done |
| IMP-018 | BodySerializableInjector matches any type when body null | HIGH | Done |
| IMP-019 | Parameters.putAll bypasses case normalization | MEDIUM | Done |
| IMP-020 | Parameters.put auto-List violates Map contract | MEDIUM | Won't Fix (by design) |
| IMP-021 | Parameters.get always returns String | MEDIUM | Won't Fix (by design) |
| IMP-022 | Parameters.of(Object...) no bounds check | LOW | Done |
| IMP-023 | NestedParamsMiddleware no depth limit | MEDIUM | Done |
| IMP-024 | Cacheable injector mapping + LambdaMetafactory | LOW | Done |
| IMP-025 | Missing test coverage for parameter injection | HIGH | Done |
| IMP-026 | MixinUtils Proxy uses Method.invoke() on hot path | MEDIUM | Done |
| IMP-027 | MixinUtils.mixin() uses Stream on hot path | LOW | Done |
| IMP-028 | CookiesMiddleware allocates HashMap when no cookies | LOW | Done |
| IMP-029 | NestedParamsMiddleware processes empty params | LOW | Done |
