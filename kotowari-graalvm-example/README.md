# kotowari-graalvm-example

A minimal GraalVM Native Image example for the enkan/kotowari framework.
It exposes a simple in-memory Todo JSON API backed by Undertow.

## Prerequisites

| Tool | Version |
|------|---------|
| GraalVM JDK | 25 (with `native-image`) |
| Maven | 3.6.3+ |

Make sure `native-image` is on your `PATH`:

```bash
native-image --version
```

## Project structure

```
kotowari-graalvm-example/
├── src/main/java/kotowari/example/graalvm/
│   ├── NativeMain.java                  # Entry point
│   ├── NativeSystemFactory.java         # EnkanSystem wiring (Undertow + Jackson)
│   ├── NativeApplicationFactory.java    # Middleware stack + route definitions
│   ├── GenerateMixinConfig.java         # Build-time helper — writes $Mixin .class files to target/classes/
│   ├── controller/
│   │   └── TodoController.java          # List / show / create actions (in-memory store)
│   ├── jaxrs/
│   │   ├── JsonBodyReader.java          # Jackson-based MessageBodyReader
│   │   └── JsonBodyWriter.java          # Jackson-based MessageBodyWriter
│   └── model/
│       └── Todo.java                    # record Todo(long id, String title, boolean done)
└── src/main/resources/META-INF/native-image/
    └── net.unit8.enkan/kotowari-graalvm-example/
        ├── native-image.properties      # Sets kotowari.routes.factory system property
        ├── reflect-config.json          # Reflection registrations for factory + model classes
        └── resource-config.json         # Resource inclusions (misconfiguration properties, Undertow/XNIO services)
```

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/todos` | List all todos |
| GET | `/todos/:id` | Get a todo by ID |
| POST | `/todos` | Create a todo (JSON body) |

## Running on the JVM (development)

```bash
mvn package -pl kotowari-graalvm-example -am
mvn exec:java -pl kotowari-graalvm-example
```

The server starts on port **3000**.

## Building a native image

```bash
mvn package -pl kotowari-graalvm-example -am -Pnative
```

The build performs two steps automatically:

1. **`prepare-package` phase** — `GenerateMixinConfig` runs via `exec-maven-plugin`.
   It builds the middleware stack, triggers `MixinUtils.createFactory()`, captures the
   generated `$Mixin` class bytes, and writes them as ordinary `.class` files into
   `target/classes/` so the Maven shade plugin includes them in the fat JAR.

2. **`package` phase** — `native-maven-plugin` invokes `native-image` with:
   - `--features=enkan.graalvm.EnkanFeature,kotowari.graalvm.KotowariFeature`
   - `--initialize-at-build-time=enkan.util.MixinUtils,...`

The output binary is `target/kotowari-graalvm-example`.

### How mixin classes are handled

`MixinUtils.createFactory()` uses the Java Class File API to generate a concrete
`DefaultHttpRequest$Mixin` subclass.  GraalVM forbids dynamic class definition at
native-image runtime, so the generated `.class` file is written to `target/classes/`
by `GenerateMixinConfig` and compiled into the fat JAR as an ordinary class.
The `MixinUtils.factoryCache` (populated during `KotowariFeature.beforeAnalysis`) is
frozen into the native image heap via `--initialize-at-build-time=enkan.util.MixinUtils`,
so the pre-built `Supplier<T>` is reused at runtime without any `defineClass` call.

## Running the native binary

```bash
./target/kotowari-graalvm-example
```

Expected output:

```
INFO: starting server: Undertow - 2.3.x.Final
...
```

## Smoke test

```bash
# List todos (empty)
curl http://localhost:3000/todos

# Create a todo
curl -X POST http://localhost:3000/todos \
     -H "Content-Type: application/json" \
     -d '{"title":"Buy milk","done":false}'

# List todos
curl http://localhost:3000/todos

# Get a single todo
curl http://localhost:3000/todos/1
```

## How GraalVM features work

### `KotowariFeature`

Runs at native-image build time (`beforeAnalysis`):

- Resolves routes via the `kotowari.routes.factory` system property
  (points to `NativeApplicationFactory.routes()`).
- Generates a `KotowariDispatcher` class using the Class File API — a static
  `dispatch(String key, Object controller, Object[] args)` method with a direct
  `if`-chain over `"ControllerClass#action"` keys, replacing all `LambdaMetafactory` usage.
- Registers all controller classes and their action methods with
  `RuntimeReflection` so they can be instantiated at runtime.
- Calls `buildApp().createRequest()` to trigger mixin class generation and
  registers the resulting `$Mixin` classes with the native image.

### `EnkanFeature`

Runs at native-image build time (`beforeAnalysis`):

- Generates a `ComponentBinder<T>` implementation for each registered component class
  using the Class File API.  The binder performs direct field writes (`putfield`) for
  `@Named @Inject` fields, eliminating reflection in component wiring.
- `NativeComponentInjector` uses these binders at runtime, delegating unnamed
  `@Inject` fields and `@PostConstruct` to the parent `ComponentInjector`.
