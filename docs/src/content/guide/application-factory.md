type=page
status=published
~~~~~~

# Application Factory

ApplicationFactory is a factory of a web application. Enkan has no configuration files.
So all of the configuration are written in ApplicationFactory.

In ApplicationFactory routing and using middleware should be described.

## Routing

Kotowari's routing has the features like RoR routing. There are recognizing and generating. 

### Defining routes

`Routes` class has the `define` method for the mapping between the path and the controller method.

```language-java
Routes routes = Routes.define(r -> {
    r.get("/").to(HomeController.class, "index");
    r.scope("/admin", admin -> {
        admin.resource(UserController.class);
    });
}).compile();
```

In the example above, `r` is a `RoutePatterns` class for defining each route.
It provides useful methods as follows:

|Method|Description|
|:---------|:------|
|get|If HTTP request method is `GET`, the request is mapped to the given controller method.|
|post|If HTTP request method is `POST`, the request is mapped to the given controller method.|
|put|If HTTP request method is `PUT`, the request is mapped to the given controller method.|
|patch|If HTTP request method is `PATCH`, the request is mapped to the given controller method.|
|delete|If HTTP request method is `DELETE`, the request is mapped to the given controller method.|
|resource|Creating a resourceful route.|
|scope|Grouping the multiple routes and prepend the path to each sub-routes.|

`resource` method will generate routes as follows:

|HTTP verb|Path      |Controller method|
|:--------|:---------|:----------------|
|GET      |/         |index            |
|GET      |/:id      |show             |
|GET      |/new      |newForm          |
|POST     |/         |create           |
|GET      |/:id/edit |edit             |
|PUT      |/:id      |update           |
|DELETE   |/:id      |delete           |

And `scope` can be nested.

```language-java
Routes routes = Routes.define(r ->
    r.scope("/admin", admin ->
        admin.scope("/user", user ->
            user.get("/list").to(AdminUserController.class, "list")))
).compile();
```

Above example routes the `/admin/user/list` request to the `list` method of AdminUserController class. 

### Generates path

`UrlRewriter#urlFor` methods can generate the path string from the given parameters.

```language-java
Routes routes = Routes.define(r -> {
    r.get("/a/b/").to(TestController.class, "index");
    r.get("/a/b/:id").to(TestController.class, "show");
}).compile();

// Generates "/a/b/"
routes.generate(OptionMap.of("controller", TestController.class, "action", "index"));

// Generates "/a/b/1"
routes.generate(OptionMap.of("controller", TestController.class, "action", "show", "id", 1));
```

## Middleware

To use and configure the middleware, call `use()` method in Application.
The middleware executes in the order they are called `use()`.


```language-java
    app.use(new DefaultCharsetMiddleware());
    app.use(new MetricsMiddleware<>());
    app.use(NONE, new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
    app.use(envIn("development"), new StacktraceMiddleware());
```

### Predicates

If the `use` method takes two arguments, its first argument is a `Predicate`.
`Predicate` is a condition whether or not the middleware is applied.

Standard predicates as follows:

|Type                    |Description                |
|:-----------------------|:--------------------------|
|NonePredicate           |Not applied to all requests|
|AnyPredicate            |Applied to all requests|
|PathPredicate           |Applied to a request that matches the given path pattern and request method|
|AuthenticatedPredicate  |Applied to a request that has valid `UserPrincipal` |
|PermissionPredicate     |Applied to a request that its `UserPrincipal` contains the given permissions|
|EnvPredicate            |Applied to a request that the given environment matches the `ENKAN_ENV` environment variable.|

