type=page
status=published
title=Enkan System | Enkan
~~~~~~

# Enkan System

Enkan system is a container of components for managing the lifecycle and dependencies.
It's inspired by [clojure component](https://github.com/stuartsierra/component).

## Usage


Enkan system is composed of components.

```java
EnkanSystem system = EnkanSystem.of(
    "http", new UndertowComponent()
);
```

Using `of` method, components are registered to Enkan system. 

### Creating components

Components must be extended `SystemComponent` class. And implements `ComponentLifecycle`.
 
```java
class SomeComponent extends SystemComponent {
    @Override
    protected ComponentLifecycle<SomeComponent> lifecycle() {
        return new ComponentLifecycle<SomeComponent>() {
            @Override
            public void start(SomeComponent component) {
            }

            @Override
            public void stop(SomeComponent component) {
            }
        }
    }
}
```

ComponentLifecycle interface have 2 methods, `start` and `stop`. Their are called sequentially.
So all components are started and stopped safely.

### Relationships between components

Components often depend on each other. For example, an application component needs a template engine and a database provider, and the database provider itself needs a datasource. You declare these relationships with `relationships()`:

```java
system.relationships(
    component("http").using("app"),
    component("app").using("template", "doma", "datasource"),
    component("doma").using("datasource"),
    component("flyway").using("datasource")
);
```

`component("X").using("Y", "Z")` means: _X depends on Y and Z_.

Enkan uses these declarations to do two things automatically:

1. **Startup ordering** — dependencies are started before the components that use them. In the example above, `datasource` starts first, then `doma` and `flyway`, then `app`, then `http`.
2. **Dependency injection** — each component's fields annotated with `@Inject` are populated with the declared dependencies before `start()` is called.

If a declared dependency is not registered in the system, Enkan throws a `MisconfigurationException` at startup — not at request time.

```
core.COMPONENT_NOT_FOUND: Component 'datasource' not found (required by 'doma')
```

### Inject components

Components are injected to a field of another component and middlewares. 

For injecting to a field, put the `Inject` annotation on it.

```java
@Inject
private TemplateEngine templateEngine;
```

If a class has multiple fields of same type, you can put the `Named` annotation for distinct. 

```java
@Named("freemarker")
@Inject
private TemplateEngine freemarker;

@Named("thymeleaf")
@Inject
private TemplateEngine thymeleaf;
```

## Bootstrap the Enkan system

To start/stop the Enkan system, call the `start()` or `stop()` method.

```java
system.start();

// ...

system.stop();
```

It is better to bootstrap a system in the REPL for the operation.

```java
PseudoRepl repl = new PseudoRepl(MyExampleSystemFactory.class.getName());
ReplBoot.start(repl,
        new KotowariCommandRegister(),
        new DevelCommandRegister(),
        new MetricsCommandRegister());
```

`PseudoRepl` is a REPL server. The factory class for Enkan system.
`ReplBoot` is a tool for starting the REPL server and registering some commands to the REPL at same time.
