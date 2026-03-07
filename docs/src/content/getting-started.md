type=page
status=published
title=Getting started | Enkan
~~~~~~

# Getting started

## Prerequisite

- Java 25 or higher
- Maven 3.6.3 or higher

## Add the dependency

Add `enkan-web` and a server component to your `pom.xml`:

```xml
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-web</artifactId>
  <version>0.13.0</version>
</dependency>
<dependency>
  <groupId>net.unit8.enkan</groupId>
  <artifactId>enkan-component-jetty</artifactId>
  <version>0.13.0</version>
</dependency>
```

## Hello World

Define your application factory with a middleware stack and a route:

```java
import enkan.Application;
import enkan.application.WebApplication;
import enkan.middleware.DefaultCharsetMiddleware;
import kotowari.middleware.ControllerInvokerMiddleware;
import kotowari.routing.Routes;

public class MyAppFactory implements ApplicationFactory {
    @Override
    public Application create(EnkanSystem system) {
        WebApplication app = new WebApplication();

        Routes routes = Routes.define(r -> {
            r.get("/").to(HomeController.class, "index");
        }).compile();

        app.use(new DefaultCharsetMiddleware());
        app.use(new RoutingMiddleware(routes));
        app.use(new ControllerInvokerMiddleware(system));
        return app;
    }
}
```

Your controller is a plain Java class — no annotations, no base class:

```java
public class HomeController {
    public HttpResponse index() {
        return HttpResponse.of("Hello, World!");
    }
}
```

Wire everything together in a system factory:

```java
import static enkan.system.EnkanSystem.component;

public class MySystemFactory {
    public EnkanSystem create() {
        return EnkanSystem.of(
            "app",  new ApplicationComponent(MyAppFactory.class.getName()),
            "http", new JettyComponent()
        ).relationships(
            component("http").using("app")
        );
    }
}
```

## Start REPL

```bash
% mvn -e compile exec:java
```

If you execute the `/start` command, application will start.

```bash
enkan> /start
```

Try to access [`http://localhost:3000/`](http://localhost:3000/) in your browser.
