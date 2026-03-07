type=page
status=published
title=Kotowari 理 | Enkan
~~~~~~

# Kotowari 理

Kotowari is a lightweight MVC web application framework on Enkan.

## Controller

A controller is a plain Java class. Its methods handle HTTP requests and return
an `HttpResponse` (or a value that a middleware converts into one).

```java
public class CustomerController {
    @Inject
    private TemplateEngine templateEngine;

    @Inject
    private DomaProvider daoProvider;

    public HttpResponse index() {
        CustomerDao dao = daoProvider.get(CustomerDao.class);
        return templateEngine.render("customer/index",
                "customers", dao.selectAll());
    }

    public HttpResponse show(Parameters params) {
        CustomerDao dao = daoProvider.get(CustomerDao.class);
        return templateEngine.render("customer/show",
                "customer", dao.selectById(Long.valueOf(params.get("id"))));
    }
}
```

### Component Injection

Components registered in the Enkan system are injected into `@Inject`-annotated
fields of a controller by `ControllerInvokerMiddleware`.

```java
@Inject
private TemplateEngine templateEngine;

@Inject
private DomaProvider daoProvider;
```

If multiple fields share the same type, use `@Named` to distinguish them:

```java
@Named("freemarker")
@Inject
private TemplateEngine freemarker;

@Named("thymeleaf")
@Inject
private TemplateEngine thymeleaf;
```

### Method Arguments

`ControllerInvokerMiddleware` injects the following types into controller method
parameters automatically, in any order:

| Type              | Description                                              |
|:------------------|:---------------------------------------------------------|
| `Parameters`      | Query string and form parameters                         |
| `UserPrincipal`   | Authenticated user (requires `AuthenticationMiddleware`) |
| `Session`         | HTTP session                                             |
| `Flash`           | One-request flash messages                               |
| `Conversation`    | Long-running conversation identifier                     |
| `ConversationState` | Key/value state scoped to the current conversation     |
| `Locale`          | Negotiated locale (requires `ContentNegotiationMiddleware`) |

## Routing

Kotowari provides Rails-style routing.

### Defining Routes

```java
Routes routes = Routes.define(r -> {
    r.get("/").to(HomeController.class, "index");
    r.get("/customers").to(CustomerController.class, "index");
    r.get("/customers/:id").to(CustomerController.class, "show");
    r.post("/customers").to(CustomerController.class, "create");
    r.resource(CustomerController.class);   // generates all CRUD routes
    r.scope("/admin", admin -> {
        admin.resource(UserController.class);
    });
}).compile();
```

#### Route Methods

| Method     | Description                                               |
|:-----------|:----------------------------------------------------------|
| `get`      | Maps GET requests to a controller method                  |
| `post`     | Maps POST requests to a controller method                 |
| `put`      | Maps PUT requests to a controller method                  |
| `patch`    | Maps PATCH requests to a controller method                |
| `delete`   | Maps DELETE requests to a controller method               |
| `resource` | Generates a full set of CRUD routes for a controller      |
| `scope`    | Groups routes under a common path prefix                  |

#### Resource Routes

`r.resource(CustomerController.class)` generates:

| HTTP verb | Path               | Controller method |
|:----------|:-------------------|:------------------|
| GET       | /customers         | index             |
| GET       | /customers/:id     | show              |
| GET       | /customers/new     | newForm           |
| POST      | /customers         | create            |
| GET       | /customers/:id/edit| edit              |
| PUT       | /customers/:id     | update            |
| DELETE    | /customers/:id     | delete            |

### Path Generation

```java
// Generates "/customers/"
routes.generate(OptionMap.of("controller", CustomerController.class, "action", "index"));

// Generates "/customers/1"
routes.generate(OptionMap.of("controller", CustomerController.class, "action", "show", "id", 1));
```

## Form Handling

`FormMiddleware` populates a form object from request parameters.

```java
public class CustomerForm extends FormBase {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Email
    private String email;
}
```

The form object is injected into the controller method by `ControllerInvokerMiddleware`:

```java
public HttpResponse create(CustomerForm form) {
    // form fields are already populated from request parameters
}
```

## Validation

`ValidateBodyMiddleware` validates the form object using Jakarta Bean Validation (JSR 380).
Validation errors are stored on the form object itself (via `Validatable`).

```java
public HttpResponse create(CustomerForm form) {
    if (!form.isValid()) {
        return templateEngine.render("customer/new", "form", form);
    }
    // proceed with valid form
}
```

## Transactions

`TransactionMiddleware` wraps controller invocation in a JTA transaction when
the controller method or class is annotated with `@Transactional`.

```java
@Transactional
public HttpResponse create(CustomerForm form) {
    CustomerDao dao = daoProvider.get(CustomerDao.class);
    dao.insert(toEntity(form));
    return redirect("/customers");
}
```

If the controller method throws a `RuntimeException`, the transaction is rolled back automatically.

## Content Negotiation (SerDes)

`SerDesMiddleware` deserializes the request body and serializes the response body
using JAX-RS `MessageBodyReader`/`MessageBodyWriter` implementations.

Add a JAX-RS provider dependency (e.g. Jackson) to enable JSON support:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.jaxrs</groupId>
    <artifactId>jackson-jaxrs-json-provider</artifactId>
</dependency>
```

A controller method can return any serializable object:

```java
public List<Customer> list() {
    return daoProvider.get(CustomerDao.class).selectAll();
}
```

`SerDesMiddleware` automatically converts it to JSON (or another format)
based on the `Accept` request header.
