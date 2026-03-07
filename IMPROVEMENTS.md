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

### IMP-034: Move Runtime `mixin()` Hot Paths to `createFactory()` Pre-Mixed Instances (HIGH)

Reduce runtime `Proxy` creation in request/response hot paths by preferring `MixinUtils.createFactory(...)` where mixins are statically known.

**Rationale**:  
`MixinUtils.mixin(...)` is still used widely in middleware execution paths. Even with caching, dynamic proxy invocation adds overhead and complexity.  
Java 25 baseline allows us to lean on the Class-File API path (`createFactory`) and shift work to startup time.

**Scope**:

- Audit middleware paths that repeatedly call `MixinUtils.mixin(...)`.
- Introduce startup-time factory assembly where mixins are predictable.
- Keep `mixin(...)` as fallback for fully dynamic cases.

**Proposed direction**:

- Expand the existing `WebApplication#createRequest()` factory strategy to other predictable object creation points.
- Define a clear rule:
  - Static mixin set -> `createFactory(...)`
  - Runtime-dependent mixin set -> `mixin(...)`

**Acceptance criteria**:

- No behavioral regression in middleware capability injection.
- Existing tests pass without semantic changes.
- Benchmark demonstrates reduced allocation/call overhead in request processing.

---

### IMP-035: Replace Long-Lived Static Mixin Caches with `ClassValue`-Based Caches (MEDIUM)

Refactor `MixinUtils` cache design to reduce classloader retention risk and simplify cache lifecycle management.

**Rationale**:  
`MixinUtils` currently relies on multiple process-wide static `ConcurrentHashMap` caches keyed by `Class<?>`, `Method`, and interface lists.  
In development/reload scenarios, this pattern can retain metadata longer than intended.

**Scope**:

- Evaluate each current cache in `MixinUtils`:
  - interface discovery cache
  - proxy constructor cache
  - method-handle caches
- Migrate class-scoped caches to `ClassValue` where appropriate.
- Keep CHM only for keys that are not naturally class-scoped.

**Proposed direction**:

- Introduce `ClassValue` for per-class interface metadata.
- Rework cache keying so that classloader boundaries are handled naturally.
- Document cache policy in `MixinUtils` JavaDoc.

**Acceptance criteria**:

- Functional parity with current `mixin(...)` and `createFactory(...)` behavior.
- No regression in current micro/benchmark numbers.
- Cache implementation is simpler to reason about for reload scenarios.

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
| IMP-008 | JWT Auth Backend | MEDIUM | Proposed |
| IMP-010 | Structured Logging Support | LOW | Proposed |
| IMP-012 | Unify Switch Expressions / Pattern Switch | HIGH | Proposed |
| IMP-013 | Introduce Sealed Classes | MEDIUM | Proposed |
| IMP-033 | Compile-time mixin resolution via bytecode generation | HIGH | Proposed |
| IMP-034 | Move runtime mixin hot paths to createFactory pre-mixed instances | HIGH | Proposed |
| IMP-035 | Replace long-lived static mixin caches with ClassValue-based caches | MEDIUM | Proposed |
