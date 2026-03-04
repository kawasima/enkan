type=page
status=published
title=REPL Commands | Enkan
~~~~~~

# REPL Commands

## Core Commands

These commands are always available in the Enkan REPL.

### /start

Start the Enkan system. All components are started in dependency order.

### /stop

Stop the Enkan system. All components are stopped in reverse dependency order.

### /reset

Stop and restart the Enkan system. This triggers class reloading
when `META-INF/reload.xml` is present in the classpath.

### /shutdown

Shut down the Enkan system and exit the REPL process.

### /help

Show the list of available commands.

### /middleware [app] list

Show the middleware stack for the specified application component.

## Devel Commands

Registered by `DevelCommandRegister`. Requires the `enkan-devel` dependency.

### /autoreset

Watch compiled class files for changes and automatically reset the application
when a modification is detected. Uses `WatchService` to monitor all
directories marked with `META-INF/reload.xml`.

See [Development Tools](../guide/development.html) for details on the class reloading mechanism.

### /compile

Compile the project using the configured build tool.
By default, uses Maven (`MavenCompiler`). Can be configured to use Gradle
by passing a `GradleCompiler` to `DevelCommandRegister`.

## Kotowari Commands

Registered by `KotowariCommandRegister`. Available when using the Kotowari web framework.

### /routes [app]

Show the routing table for the specified application component.

## Metrics Commands

Registered by `MetricsCommandRegister`. Requires the `enkan-component-metrics` dependency.

### /metrics

Display collected application metrics from the Metrics component.
