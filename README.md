# enkan

Enkan(円環) is a microframework implementing a middleware pattern like ring or connect.

[![CircleCI](https://circleci.com/gh/kawasima/enkan.svg?style=svg&circle-token=e3d88ba4abde99dabc9fe527d0681d236ff49548)](https://circleci.com/gh/kawasima/enkan)

## Concept

- Minimal (Simple made easy)
- Ease of development
- Ease of operation

### Minimal

- Middleware pattern
- No configuration files
- Avoid blackbox
- Less annotations
- Less library dependencies
- Single instance (Middlewares and compoments, controller)

### Ease of development

- Faster startup (Preventing to scan classes)
- Reloading classes without restarting JVM
- Trace execution of middlewares
- Alert misconfiguration

### Ease of operation

- Starting server is fast. (~3 seconds)
- Resetting application is very fast. (~1 seconds)
- Run-time change predicates of middleware on the REPL

## Requirements

- Java8 or higher
- Java EE 7 Specification

## Middleware

`Middleware` is an implementation of filters and chains.

- Service Unavailable
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

In Enkan, `component` is an object manages lifecycle of stateful objects.

- HikariCP
- Flyway
- Freemarker
- Thyemeleaf
- Jetty
- Undertow
- Doma2
- JPA(EclipseLink)
- Jackson
- S2Util-beans
- Metrics

Using enkan and kotowari, the following will be your code

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

## Get started

Add sonatype snapshots repository to your pom.xml

```xml
  <repositories>
    <repository>
      <id>sonatype-snapshot</id>
      <url>http://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```

## Manual

### EnkanSystem

The Enkan system consist of components. A component is a singleton instance that shares data between requests.

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

### REPL

Enkan system is operated by a REPL interface.

- Start a server
```
REPL> /start
[pool-1-thread-1] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-0 - is starting.
[pool-1-thread-1] INFO org.flywaydb.core.internal.util.VersionPrinter - Flyway 3.2.1 by Boxfuse
[pool-1-thread-1] INFO org.flywaydb.core.internal.dbsupport.DbSupportFactory - Database: jdbc:h2:mem:test (H2 1.4)
[pool-1-thread-1] INFO org.flywaydb.core.internal.command.DbValidate - Validated 1 migration (execution time 00:00.019s)
[pool-1-thread-1] INFO org.flywaydb.core.internal.metadatatable.MetaDataTableImpl - Creating Metadata table: "PUBLIC"."schema_version"
[pool-1-thread-1] INFO org.flywaydb.core.internal.command.DbMigrate - Current version of schema "PUBLIC": << Empty Schema >>
[pool-1-thread-1] INFO org.flywaydb.core.internal.command.DbMigrate - Migrating schema "PUBLIC" to version 1 - CreateCustomer
[pool-1-thread-1] INFO org.flywaydb.core.internal.command.DbMigrate - Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.059s).
2 02, 2016 7:58:35 午後 org.hibernate.validator.internal.util.Version <clinit>
INFO: HV000001: Hibernate Validator 5.2.2.Final
[pool-1-thread-1] INFO org.eclipse.jetty.util.log - Logging initialized @2688228ms
[pool-1-thread-1] INFO org.eclipse.jetty.server.Server - jetty-9.3.5.v20151012
REPL> [pool-1-thread-1] INFO org.eclipse.jetty.server.ServerConnector - Started ServerConnector@5325abc3{HTTP/1.1,[http/1.1]}{0.0.0.0:3000}
[pool-1-thread-1] INFO org.eclipse.jetty.server.Server - Started @2688295ms
```
- Stop a server
- Reload an application
- Show routing information
```
REPL> /routes app
GET    /                                        {controller=class kotowari.example.controller.ExampleController, action=index}
GET    /m1                                      {controller=class kotowari.example.controller.ExampleController, action=method1}
GET    /m2                                      {controller=class kotowari.example.controller.ExampleController, action=method2}
GET    /m3                                      {controller=class kotowari.example.controller.ExampleController, action=method3}
GET    /m4                                      {controller=class kotowari.example.controller.ExampleController, action=method4}
POST   /login                                   {controller=class kotowari.example.controller.LoginController, action=login}
```
- Show middleware stack
```
REPL> /middleware app list
ANY   defaultCharset (enkan.middleware.DefaultCharsetMiddleware@4929dbc3)
NONE   serviceUnavailable (enkan.middleware.ServiceUnavailableMiddleware@2ee4fa3b)
ANY   stacktrace (enkan.middleware.StacktraceMiddleware@545872dd)
ANY   trace (enkan.middleware.TraceMiddleware@1c985ffd)
ANY   contentType (enkan.middleware.ContentTypeMiddleware@1b68686e)
ANY   httpStatusCat (enkan.middleware.HttpStatusCatMiddleware@12d47c1a)
ANY   params (enkan.middleware.ParamsMiddleware@58d3a07)
ANY   normalization (enkan.middleware.NormalizationMiddleware@5b34eafc)
ANY   cookies (enkan.middleware.CookiesMiddleware@347c2ec)
ANY   session (enkan.middleware.SessionMiddleware@32424a32)
ANY   resource (enkan.middleware.ResourceMiddleware@5e73037f)
ANY   routing (kotowari.middleware.RoutingMiddleware@226c7147)
ANY   domaTransaction (enkan.middleware.DomaTransactionMiddleware@1f819744)
ANY   form (kotowari.middleware.FormMiddleware@3f325d5c)
ANY   validateForm (kotowari.middleware.ValidateFormMiddleware@791cd93e)
ANY   htmlRenderer (enkan.middleware.HtmlRenderer@383b6913)
ANY   controllerInvoker (kotowari.middleware.ControllerInvokerMiddleware@2b13e2e7)
```
- Rewrite a predicate of middleware
```
REPL> /middleware app predicate serviceUnavailable ANY
REPL> /middleware app list
ANY   defaultCharset (enkan.middleware.DefaultCharsetMiddleware@4929dbc3)
ANY   serviceUnavailable (enkan.middleware.ServiceUnavailableMiddleware@2ee4fa3b)
ANY   stacktrace (enkan.middleware.StacktraceMiddleware@545872dd)
```


Enkan REPL can attach to a running process.

```
enkan> /connect 35677
Connected to server (port = 35677)
```

If you use Java9 or higher, you can use JShellRepl. It's so great experience!!

```
enkan> /connect 64815
Connected to server (port = 64815)
enkan> system
EnkanSystem {
  "datasource":   #HikariCPComponent {
    "jdbcUrl": "jdbc:h2:mem:test",
    "username": "null",
    "dependencies": []
  },
  "flyway":   enkan.component.flyway.FlywayMigration@345f69f3,
  "doma":   #DomaProvider {
    "dependencies": ["flyway", "datasource"]
  },
  "jwt":   net.unit8.bouncr.sign.JsonWebToken@3f57bcad,
  "jackson":   #JacksonBeansConverter {
    "dependencies": []
  },
  "template":   enkan.component.freemarker.FreemarkerTemplateEngine@1e8b7643,
  "app":   #ApplicationComponent {
    "application": "null",
    "factoryClassName": "net.unit8.rascaloid.RascaloidApplicationFactory",
    "dependencies": ["template", "doma", "jackson", "datasource", "jwt"]
  },
  "http":   enkan.component.jetty.JettyComponent@7d286fb6
}

enkan> system.getComponent("doma")
#DomaProvider {
  "dependencies": ["flyway", "datasource"]
}
```


### Kotowari

Kotowari is a web routing framework on Enkan.

It provides a rails-like syntax for routing definition.

```java
Routes routes = Routes.define(r -> {
    r.get("/").to(ExampleController.class, "index");
    r.get("/m1").to(ExampleController.class, "method1");
    r.get("/m2").to(ExampleController.class, "method2");
    r.get("/m3").to(ExampleController.class, "method3");
    r.get("/m4").to(ExampleController.class, "method4");
    r.post("/login").to(LoginController.class, "login");
    r.resource(CustomerController.class);
}).compile();
```

## Get started

kotowari-archetype is very useful at starting point.

```sh
% bash <(curl -L https://raw.githubusercontent.com/kawasima/kotowari-archetype/master/kotowari.sh)
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  2051  100  2051    0     0   5962      0 --:--:-- --:--:-- --:--:--  5944

╔═╗┌┐┌┬┌─┌─┐┌┐┌ ┬ ╦╔═┌─┐┌┬┐┌─┐┬ ┬┌─┐┬─┐┬
║╣ │││├┴┐├─┤│││┌┼─╠╩╗│ │ │ │ ││││├─┤├┬┘│
╚═╝┘└┘┴ ┴┴ ┴┘└┘└┘ ╩ ╩└─┘ ┴ └─┘└┴┘┴ ┴┴└─┴

Which web server component do you use?:
1) undertow
2) jetty
3) No thank you
#?
```


## License

Copyright © 2016-2018 kawasima

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
