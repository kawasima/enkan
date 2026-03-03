# Agents

## Language

All source code, comments, Javadoc, and documentation in this project must be written in English.

## Design Principles

Enkan values **explicitness**, **ease of development**, and **ease of operation**. All contributions must follow these principles.

### Explicitness over magic

- Do not introduce configuration files (XML, YAML, properties, etc.). Configuration is done in code.
- Minimize the use of dependency injection. Use `@Inject` only when component injection is truly needed.
- Minimize the use of annotations. Do not introduce custom annotations for implicit behavior (e.g., auto-registration, classpath scanning).
- Prefer explicit wiring in application factories over convention-based discovery.
- Every middleware, route, and component should be visible in the application factory code.

### Ease of development

- Keep startup time fast. Avoid heavy initialization or classpath scanning at startup.
- Support hot reloading. Do not use patterns that prevent class reloading (e.g., static final caches that hold Class references across reloads).
- Use `FalteringEnvironmentException` for misconfiguration errors so developers get clear, actionable feedback.

### Ease of operation

- Components should be controllable via the REPL (start, stop, inspect).
- Keep dependencies minimal. Do not add dependencies unless they are essential.

## Coding Standards

### Security

- Use `MessageDigest.isEqual()` for constant-time comparison of security tokens (CSRF tokens, HMAC values) to prevent timing attacks.
- Do not use `String.equals()` to compare security-sensitive values.
- The `CorsMiddleware` default configuration (`credentials=true` with `origins=["*"]`) is intentionally preserved for backward compatibility, but this combination is invalid per the CORS spec and browsers will reject such responses. Always set explicit allowed origins in production.
- Use `regionMatches(true, ...)` for case-insensitive prefix matching on HTTP header values instead of `toLowerCase()` + `startsWith()`.

### Collections and Data Structures

- Use `Set.of(...)` for immutable constant sets (e.g., allowed HTTP methods).
- Do not use double-brace initialization (`new HashSet<>() {{ add(...); }}`); use `Set.of()` or a static initializer block instead.
- Return `Collections.unmodifiableSet()` from methods that expose internal sets to prevent unintended external mutation.
- Use `EnumSet.noneOf(...)` instead of `HashSet` when the element type is an enum.

### Session and Stores

- `MemoryStore` uses a TTL-based expiry (default 30 minutes) with periodic background cleanup every 60 seconds.
- `MemoryStore` implements `Closeable`; shut it down via `close()` when the application stops.
- Adjust TTL via `setTtlSeconds(long)` before the store is put into use.

### Middleware

- Prefer `contains("charset=")` (or `toLowerCase().contains(...)`) over `String.matches(...)` for substring checks; `matches()` requires a full-string regex match.
- Inline comment style for non-obvious logic: use `//` with a clear English explanation on the same or preceding line.
- Log misconfiguration warnings at most once per middleware instance using a `volatile boolean` flag; do not emit the same warning on every request.
