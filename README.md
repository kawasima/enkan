# enkan

Enkan(円環) is a microframework implemented Middleware pattern like ring or connect.  

## Concept

- Minimal
- Ease of development
- Ease of operation

### Minimal

- Middleware pattern
- No configuration files
- Less annotations
- Single instance (Middlewares and compoments, controller) 

### Ease of development

- Faster startup (Preventing to scan classes)
- Reloading classes without restarting JVM
- Trace execution of middlewares
- Alert misconfiguration

### Ease of operation

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

Using enkan and kotowari, your code is

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

