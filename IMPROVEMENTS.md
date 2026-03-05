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

## Parameter Injection

---

### IMP-018: BodySerializableInjector matches any type when body is null (HIGH)

`isApplicable()` returns `true` for any type when `getDeserializedBody()` is null. This means any unknown controller parameter type silently receives null via the fallback injector, hiding misconfiguration errors.

**File**: `kotowari/src/main/java/kotowari/inject/parameter/BodySerializableInjector.java`

---

### IMP-019: Parameters.putAll bypasses case-insensitive key normalization (MEDIUM)

`putAll()` delegates directly to the inner HashMap, skipping the `toLowerCase` normalization applied by `put()`. When `caseSensitive=false`, merged keys may not match existing entries.

**File**: `enkan-core/src/main/java/enkan/collection/Parameters.java`

---

### IMP-020: Parameters.put auto-converts to List, violating Map contract (MEDIUM)

Calling `put(key, value)` twice with the same key creates a List instead of replacing the value. This violates the `Map.put` contract and causes unexpected behavior when `putAll` is used to merge query and form params.

**File**: `enkan-core/src/main/java/enkan/collection/Parameters.java`

---

### IMP-021: Parameters.get always returns String via toString() (MEDIUM)

`get()` calls `val.toString()` on any stored value. For nested Parameters or Lists, this returns representation strings like `{foo=bar}` instead of the actual object. The return type is `String` but the `Map<String, Object>` contract expects `Object`.

**File**: `enkan-core/src/main/java/enkan/collection/Parameters.java`

---

### IMP-022: Parameters.of(Object...) has no bounds check (LOW)

Passing an odd number of arguments causes `ArrayIndexOutOfBoundsException`. Should throw `MisconfigurationException` with a clear message (the key `core.MISSING_KEY_VALUE_PAIR` already exists).

**File**: `enkan-core/src/main/java/enkan/collection/Parameters.java`

---

### IMP-023: NestedParamsMiddleware has no depth limit (MEDIUM)

`assocNested` uses recursion with no depth limit. A crafted parameter name like `a[b][c][d]...[z]` with hundreds of nesting levels can cause `StackOverflowError`.

**File**: `enkan-web/src/main/java/enkan/middleware/NestedParamsMiddleware.java`

---

### IMP-025: Missing test coverage for parameter injection pipeline (HIGH)

No tests exist for `ParamsMiddleware` or `BodySerializableInjector`. `NestedParamsMiddleware` has 3 test cases with no edge-case coverage. `Parameters` has 1 test case.

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
| IMP-018 | BodySerializableInjector matches any type when body null | HIGH | Proposed |
| IMP-019 | Parameters.putAll bypasses case normalization | MEDIUM | Proposed |
| IMP-020 | Parameters.put auto-List violates Map contract | MEDIUM | Proposed |
| IMP-021 | Parameters.get always returns String | MEDIUM | Proposed |
| IMP-022 | Parameters.of(Object...) no bounds check | LOW | Proposed |
| IMP-023 | NestedParamsMiddleware no depth limit | MEDIUM | Proposed |
| IMP-024 | Cacheable injector mapping + LambdaMetafactory | LOW | Done |
| IMP-025 | Missing test coverage for parameter injection | HIGH | Partial |
| IMP-026 | MixinUtils Proxy uses Method.invoke() on hot path | MEDIUM | Done |
