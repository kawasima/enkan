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
