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

