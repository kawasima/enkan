# enkan

Enkan(円環) is a microframework implemented middleware pattern like ring or connect.  

## Concept

- Minimal
- Ease of development
- Ease of operation

### Minimal

- Middleware pattern
- No configuration files
- Less annotations
- Less library dependencies
- Single instance (Middlewares and compoments, controller) 

### Ease of development

- Faster startup (Preventing to scan classes)
- Reloading classes without restarting JVM
- Trace execution of middlewares
- Alert misconfiguration

### Ease of operation

- Run-time change predicates of middleware on the REPL

## Requirements

- Java8
- Java EE 7 Specification

## Middleware

- Session
- Flash
- Cookie
- Parsing parameters
- Trace log
- Populating form (kotowari) 
- JSR-303 Validation (kotowari)
- Routing like Rails (kotowari)
- Injecting components to a controller (kotowari)

## Components

- HikariCP
- Flyway
- Freemarker
- Jetty
- Doma2

Using enkan and kotowari, your code is following

```java
public class ExampleController {
    @Inject
    private TemplateEngineComponent templateEngine;

    @Inject
    private DomaDaoProvider daoProvider;

    public HttpResponse index(ExampleForm form) {
        CustomerDao customerDao = daoProvider.get(CustomerDao.class);
        Customer customer = customerDao.selectById(form.getId());
        return templateEngine.render("example",
                "customer", customer);
    }
}
```

## Manual

### EnkanSystem

Enkan system is consist of components. Component is a singleton instance sharing data between requests.

```java
EnkanSystem.of(
    "doma", new DomaProvider(),
    "flyway", new FlywayMigration(),
    "template", new FreemarkerComponent(),
    "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test")),
    "app", new ApplicationComponent("kotowari.example.MyApplicationFactory"),
    "http", builder(new JettyComponent())
        .set(JettyComponent::setPort, Env.getInt("PORT", 3000))
        .build()
).relationships(
    component("http").using("app"),
    component("app").using("template", "doma", "datasource"),
    component("doma").using("datasource"),
    component("flyway").using("datasource")
);
```

### Application

An application has a stack of middlewares.
A middleware is a single instance. By `use` method, the middleware is used by application.

```java
app.use(ANY("/secret"), new AuthenticateMiddleware());
```
