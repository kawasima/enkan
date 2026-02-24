type=page
status=published
title=Development Tools | Enkan
~~~~~~

# Development Tools

Enkan provides a set of development tools that enable rapid iterative development.
The `enkan-devel` module includes class reloading, REPL compilation, automatic reset,
error visualization, and request tracing.

## Class Reloading

Enkan supports hot-reloading of Java classes without restarting the JVM.
This is achieved by replacing the ClassLoader each time the application resets.

### How It Works

1. **ConfigurationLoader** is a custom `ClassLoader` that identifies reloadable classpath directories.
   A directory is considered reloadable if it contains `META-INF/reload.xml`.

2. When the application starts, `ApplicationComponent` creates a new `ConfigurationLoader`
   and loads the `ApplicationFactory` class through it.

3. When the application resets (`/reset`), the old `ConfigurationLoader` is discarded
   and a new one is created. Since a fresh ClassLoader reads `.class` files from disk,
   any recompiled classes are picked up automatically.

### Setting Up Reloading

Place an empty `META-INF/reload.xml` file in your resources directory:

```
src/main/resources/META-INF/reload.xml
```

This marks the corresponding output directory (e.g., `target/classes`) as a reloading target.
Only classes in directories containing this marker file will be reloaded.

### What Gets Reloaded

- Application factory classes
- Controller classes
- Any class loaded from a directory marked with `META-INF/reload.xml`

Classes from JAR dependencies are **not** reloaded. They are delegated to the parent ClassLoader.

## Auto Reset

The `/autoreset` command watches compiled class files for changes and automatically
resets the application when a modification is detected.

### Usage

```
enkan> /autoreset
Start to watch modification an application.
```

Once enabled, whenever a `.class` file changes in a reloadable directory,
the system automatically calls `stop()` followed by `start()`, reloading all classes.

### How It Works

`ClassWatcher` uses Java NIO `WatchService` to monitor reloadable directories.
It registers watches recursively on all subdirectories and detects `ENTRY_CREATE`
and `ENTRY_MODIFY` events. New subdirectories created after watching starts are
automatically registered.

### Typical Workflow

1. Start the application: `/start`
2. Enable auto reset: `/autoreset`
3. Edit Java source files in your IDE
4. Your IDE (or `/compile`) compiles the sources
5. ClassWatcher detects the changed `.class` files
6. The application resets automatically with the new code

## Compile Command

The `/compile` command triggers project compilation from the REPL.

### Maven (default)

```
enkan> /compile
```

`MavenCompiler` invokes Maven's `compile` goal using the Maven Invoker API.
It reads `MAVEN_HOME` or `M2_HOME` environment variable (falls back to `/opt/maven`).

### Gradle

To use Gradle instead of Maven, configure `DevelCommandRegister` with a `GradleCompiler`:

```java
DevelCommandRegister devel = new DevelCommandRegister(new GradleCompiler());
```

`GradleCompiler` uses the Gradle Tooling API to invoke the `compileJava` task.
By default, it downloads Gradle 8.14.4 automatically. You can override this by setting
the `GRADLE_HOME` environment variable to use a local installation.

## Development Middlewares

The `enkan-devel` module provides middlewares designed for use during development.
These should not be included in production deployments.

### Usage

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-devel</artifactId>
</dependency>
```

### StacktraceMiddleware

Catches exceptions and renders detailed HTML error pages with stack traces.

```java
app.use(new StacktraceMiddleware<>());
```

It handles different exception types with specialized pages:

- **MisconfigurationException** - Shows a configuration problem summary with actionable details
- **UnreachableException** - Indicates a code path that should never be reached
- **Other exceptions** - Renders a full stack trace with source context

### TraceWebMiddleware

Records HTTP request/response traces and provides a web UI to inspect them.

```java
app.use(new TraceWebMiddleware<>());
```

Access the trace UI at `http://localhost:3000/x-enkan/requests` to see:

- A list of recent requests (up to 100 by default)
- Detailed view for each request showing headers, parameters, and response info
- Per-middleware execution timing (inbound and outbound)

The mount path and store size are configurable:

|Name|Type|Description|Default|
|:---|:---|:---------|:------|
|mountPath|String|URL path for trace UI|/x-enkan/requests|
|storeSize|long|Maximum number of stored requests|100|

### HttpStatusCatMiddleware

Renders HTTP status cat images for error responses. Useful for quickly identifying
status codes during development.

```java
app.use(new HttpStatusCatMiddleware());
```

## Registering Development Commands

Development commands are registered via `DevelCommandRegister` when bootstrapping the REPL:

```java
PseudoRepl repl = new PseudoRepl(MySystemFactory.class.getName());
ReplBoot.start(repl,
        new KotowariCommandRegister(),
        new DevelCommandRegister(),
        new MetricsCommandRegister());
```

`DevelCommandRegister` registers the following commands:

|Command|Description|
|:------|:---------|
|/autoreset|Watch class files and reset on change|
|/compile|Compile project using Maven or Gradle|
