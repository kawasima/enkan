type=page
status=published
~~~~~~

# Enkan System

Enkan system is a container of components for managing the lifecycle and dependencies.
It's inspired by [clojure component](https://github.com/stuartsierra/component).

## Usage


```language-java
EnkanSystem system = EnkanSystem.of(
    "http", new UndertowComponent()
);
```

Enkan system

### Creating components

Components must be extended `SystemComponent` class. And implements `ComponentLifecycle`.
 
```language-java
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

### Inject components

Components are injected to a field of another component and middlewares. 

For injecting to a field, put the `Inject` annotation on it.

```language-java
@Inject
private TemplateEngine templateEngine;
```

If a class has multiple fields of same type, you can put the `Named` annotation for distinct. 

```language-java
@Named("freemarker")
@Inject
private TemplateEngine freemarker;

@Named("thymeleaf")
@Inject
private TemplateEngine freemarker;
```

## Bootstrap the Enkan system

To start/stop the Enkan system, call the `start()` or `stop()` method.

```language-java
system.start();

// ...

system.stop();
```

It is better to bootstrap a system in the REPL for the operation.

```language-java
PseudoRepl repl = new PseudoRepl(MyExampleSystemFactory.class.getName());
ReplBoot.start(repl,
        new KotowariCommandRegister(),
        new DevelCommandRegister(),
        new MetricsCommandRegister());
```

`PseudoRepl` is a REPL server. The factory class for Enkan system.
`ReplBoot` is a tool for starting the REPL server and registering some commands to the REPL at same time.
