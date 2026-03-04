# enkan Project

## Overview

enkan is a middleware-chain-based web framework for Java 21.
Maven multi-module project (`enkan-parent` version `0.12.0`).

## Module Structure

| Module | Role |
| ------ | ---- |
| `enkan-core` | Core types and interfaces (`HttpRequest`, `HttpResponse`, `Middleware`, etc.) |
| `enkan-web` | Web middleware collection |
| `enkan-system` | `EnkanSystem`, `Repl` interface |
| `enkan-repl-server` | `JShellRepl` implementation (ZeroMQ + JShell) |
| `enkan-repl-client` | JLine-based REPL client |
| `kotowari` | MVC routing and controller invocation |
| `kotowari-jpa` | JPA `EntityManager` parameter injector |
| `enkan-devel` | Development commands (autoreset, compile) |
| `enkan-throttling` | Token Bucket-based rate limiting |
| `enkan-component-*` | Various components (JPA, Thymeleaf, Jetty, Metrics, etc.) |

## Key Architectural Knowledge

### JShellRepl Command Types

- `registerCommand(name, command)`: Serializes the command into the JShell environment. `SystemCommand implements Serializable` is required. Can operate on the `system` variable inside JShell.
- `registerLocalCommand(name, command)`: Executes on the host JVM side. No serialization needed. Can reference non-serializable objects like `Repl`.
- `eval(statement, transport)`: Executes a statement inside JShell from the host side. Use this when you need to manipulate JShell-internal variables (e.g., `system.stop(); system.start()` in `AutoResetCommand`).

### kotowari Parameter Injection

- `ParameterUtils.getDefaultParameterInjectors()` returns a new `LinkedList` each time (not a shared instance).
- Default injectors: `HttpRequest`, `Parameters`, `Session`, `Flash`, `Principal`, `Conversation`, `ConversationState`, `Locale`.
- `EntityManagerInjector` is NOT in the default list. Add it explicitly when using the `kotowari-jpa` module.

### MixinUtils Pattern

Add capabilities to request objects via `MixinUtils.mixin(request, SomeInterface.class)`.
Examples: `EntityManageable`, `BodyDeserializable`, `Routable`, `ContentNegotiable`.

## Build and Test

```sh
# Clean build (avoids incremental build cache issues)
mvn clean test

# Specific module only
mvn -pl kotowari clean test
```

**Note**: Running `mvn test` without `clean` can cause test failures with `Unresolved compilation problems` due to stale ECJ incremental build cache. Always try `mvn clean test` first when tests fail unexpectedly.

## Past Issues and Solutions

### enkan-devel: AutoResetCommand Serialization Problem

- Commands registered via `registerCommand()` are serialized into the JShell environment.
- `AutoResetCommand` needs a reference to `Repl`, which is not serializable.
- **Fix**: Register via `registerLocalCommand()` and use `repl.eval("system.stop(); system.start()", transport)` to operate on the JShell-internal system.

### enkan-devel: CompileCommand EvalException

- When executed inside JShell via `registerCommand()`, exceptions are wrapped as `EvalException`.
- The compile command does not need to run inside JShell.
- **Fix**: Changed to `registerLocalCommand()`, making the `transient` modifier on the `compiler` field unnecessary.

### enkan-repl: JShell Method Completion Broken

- `CompletionServer` was mangling the anchor value (`anchor[0] += cursor + 1`).
- `RemoteCompleter` was passing only the suffix (continuation) as a `Candidate`, missing the prefix.
- **Fix**: Server sends the raw `anchor` as a ZMQ frame; client constructs `prefix = buffer.substring(0, anchor)` and appends the continuation.

### enkan-throttling: Double-Consume Bug

- `tryConsume()` already consumes a token, but `consume()` was called afterward, consuming twice.
- **Fix**: Replaced with a single `return !bucket.tryConsume(1)`.

### kotowari: TransactionMiddleware Missing Rollback

- If `chain.next(req)` throws a `RuntimeException`, `tm.rollback()` was never called, leaving the transaction hanging.
- **Fix**: Added `catch (RuntimeException e)` block that calls `tm.rollback()` before re-throwing.

### kotowari: RenderTemplateMiddleware ClassCastException

- `Stream.of(request).filter(Objects::nonNull).map(PrincipalAvailable.class::cast)` throws `ClassCastException` when request does not implement `PrincipalAvailable`.
- **Fix**: Changed to `filter(PrincipalAvailable.class::isInstance)`.
