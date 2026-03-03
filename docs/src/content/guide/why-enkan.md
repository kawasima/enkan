type=page
status=published
title=Why Enkan? | Enkan
~~~~~~

# Why Enkan?

Enkan is a minimal Java web framework inspired by [Ring](https://github.com/ring-clojure/ring) (Clojure) and
[Connect](https://github.com/senchalabs/connect) (Node.js).
It is built around one conviction: **a web application should be explicit, traceable, and operable — not magical.**

Most mainstream Java frameworks (Spring Boot, Quarkus, Jakarta EE) excel at hiding complexity.
Enkan takes the opposite approach: it makes every piece of request processing visible, composable, and inspectable.

---

## Core Philosophy

### Explicitness over Convention

| Concern | Typical framework | Enkan |
|---------|------------------|-------|
| Configuration | `application.yml`, classpath scanning | Plain Java code — no files |
| Middleware ordering | Implicit (annotation-driven, auto-configured) | Explicit `app.use(...)` calls |
| Capabilities on request | Always present (e.g. `getSession()` always available) | Added by middleware — absent until you `use(new SessionMiddleware())` |
| Startup surprises | Framework injects beans you didn't expect | Only what you wired |

Because there are no configuration files, your IDE can rename, refactor, and statically analyse the entire application setup.

### Anti-Blackbox

When a request arrives, you can read the middleware stack from top to bottom in a single Java file and follow every transformation.
There is no AOP weaving, no annotation processor magic, and no "auto-configuration" that silently adds behaviour.

The REPL lets you inspect the live stack at any time:

```
enkan> /middleware app list
ANY   defaultCharset   (enkan.middleware.DefaultCharsetMiddleware@4929dbc3)
ANY   trace            (enkan.middleware.TraceMiddleware@1c985ffd)
ANY   session          (enkan.middleware.SessionMiddleware@32424a32)
ANY   routing          (kotowari.middleware.RoutingMiddleware@226c7147)
```

### Fewer Annotations

Annotations are useful, but overuse turns code into configuration disguised as code.
Enkan uses annotations only where compile-time metadata is genuinely needed (e.g. `@Middleware` to declare middleware dependencies).
Controllers, components, and middleware are plain Java classes.

---

## Type-Safe Middleware Composition

This is the most technically distinctive aspect of Enkan.

The `Middleware` interface carries four type parameters:

```java
interface Middleware<REQ, RES, NREQ, NRES> {
    <NNREQ, NNRES> RES handle(REQ req, MiddlewareChain<NREQ, NRES, NNREQ, NNRES> chain);
}
```

`REQ`/`RES` are the types this middleware accepts; `NREQ`/`NRES` are the types it passes to the next middleware.
A middleware that adds session support can _change_ the type of the request object, and the compiler verifies that downstream middleware actually expects the enriched type.

In frameworks that use raw `HttpServletRequest`, the compiler cannot catch a middleware being placed in the wrong order.
Enkan catches such errors at compile time.

---

## Capability-Based Request Enrichment (Mixin Pattern)

When `SessionMiddleware` runs, it dynamically adds the `WebSessionAvailable` interface to the request object via a JDK proxy:

```java
request = MixinUtils.mixin(request, WebSessionAvailable.class);
```

Until that middleware has run, `getSession()` does not exist on the request.
This makes **which capabilities are available** a function of which middleware you have registered — not a fixed API.
The result is that:

- You cannot accidentally call `getSession()` before `SessionMiddleware` has run (it would be a compile error).
- Reading the middleware stack tells you exactly what each downstream handler can do.

---

## Component System with Explicit Lifecycle

Enkan cleanly separates two concerns that many frameworks conflate:

| Concern | Enkan concept |
|---------|--------------|
| Stateful services (DB pool, template engine, HTTP server) | `SystemComponent` managed by `EnkanSystem` |
| Per-request processing logic | `Middleware` in the application stack |

Dependencies between components are declared explicitly:

```java
EnkanSystem.of(
    "datasource", new HikariCPComponent(...),
    "doma",       new DomaProvider(),
    "template",   new FreemarkerComponent(),
    "app",        new ApplicationComponent("com.example.MyAppFactory"),
    "http",       new JettyComponent()
).relationships(
    component("http").using("app"),
    component("app").using("template", "doma", "datasource"),
    component("doma").using("datasource")
);
```

The system starts components in dependency order and stops them in reverse.
There is no classpath scanning to discover components — the graph is the documentation.

---

## Ease of Development

### Hot Reload Without Restart

Changing a controller or middleware class triggers a sub-second application reset (not a JVM restart).
The component system restarts only the application layer, keeping long-initialisation resources (DB pools, etc.) alive.

```
enkan> /reset          # ~1 second
```

### Friendly Misconfiguration Errors

`@Middleware(dependencies = {"cookies"})` on `SessionMiddleware` means the system will
reject an application stack that places `SessionMiddleware` before `CookiesMiddleware` at startup,
with a clear message — not a `NullPointerException` at request time.

### Built-In Request Tracing

`TraceMiddleware` records timestamps at each layer. The accumulated trace is written to the
`X-Enkan-Trace` response header, giving you a per-request flamegraph without external tooling.

---

## Ease of Operation

### Fast Startup

JVM startup is fast because there is no classpath scanning.
A typical Enkan application starts in under 3 seconds, including database migrations.

### REPL-Driven Operations

```
enkan> /start                              # start the system
enkan> /stop                               # stop gracefully
enkan> /reset                              # hot-reload application
enkan> /routes app                         # inspect routing table
enkan> /middleware app predicate serviceUnavailable ANY   # toggle maintenance mode live
enkan> /connect 64815                      # attach to a running process via JShell
```

Runtime predicate changes mean you can enable maintenance mode, toggle feature flags,
or inspect internal state without redeployment.

---

## Comparison with Other Frameworks

| | Enkan | Spring Boot | Quarkus | Vert.x |
|---|---|---|---|---|
| Configuration | Java code only | YAML + annotations | properties + annotations | Java + JSON |
| Classpath scanning | None | Extensive | Build-time | None |
| Middleware type safety | Compile-time (generics) | Runtime (Filter chain) | Runtime | Runtime |
| Component lifecycle | Explicit graph | `@Bean` + `@Autowired` | CDI / `@ApplicationScoped` | Verticle |
| REPL / live inspection | Built-in | Actuator (HTTP) | Dev UI | – |
| Reactive / async | Opt-in | WebFlux opt-in | Reactive opt-in | Core feature |
| Target scale | Small–medium apps | All scales | Cloud-native | High-concurrency |

Enkan is not trying to replace Spring Boot for large enterprise systems.
It is a deliberate choice for teams that value **clarity and control** over
**convention and automation**.

---

## Kotowari

[Kotowari](../kotowari.md) is a Rails-like web framework built on top of Enkan.
It adds:

- Declarative routing (`r.resource(CustomerController.class)`)
- Automatic request body deserialization into form / entity objects
- JSR-303 bean validation integration
- Template rendering (Freemarker, Thymeleaf)

Kotowari inherits Enkan's philosophy: it is explicit, thin, and composed entirely of middleware.
