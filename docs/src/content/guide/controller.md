type=page
status=published
title=Controllers | Enkan
~~~~~~

# Controllers

## Component Injection

Components that are registered in the Enkan system is injected to an annotated field with `Inject` of a controller.

```java
public class UserController {
    @Inject
    private TemplateEngine templateEngine;
    
    @Inject
    private DomaProvider daoProvider;
}
```


## Arguments

Arguments of a controller method are available as follows:

- Parameters
- UserPrincipal
- Session
- Flash
- Conversation
- ConversationState
- Locale
- Request body object

If they are in the controller method parameters, `ControllerInvokerMiddleware` injects to the arguments.  
They are in random order.

```java
public class ArgumentInjectionController {
    public String method1(Parameters params, UserPrincipal principal, Session session) {

    }
}
```

### Parameters

`Parameters` is a map represents a query string and a url encoded form.

### UserPrincipal

`UserPrincipal` represents an authenticated user. It is available when `AuthenticationMiddleware` is used.

### Session

`Session` is a map-like object injected from the request. Read values with `session.get("key")`.

To persist session changes, set the modified session on the response — mutating the injected object alone is not enough:

```java
public HttpResponse login(Parameters params, Session session) {
    session.put("userId", params.get("username"));
    return builder(redirect("/dashboard"))
        .set(HttpResponse::setSession, session)
        .build();
}
```

Reading session data without changing it does not require setting it back on the response:

```java
public HttpResponse dashboard(Session session) {
    String userId = (String) session.get("userId");
    if (userId == null) return redirect("/login");
    return templateEngine.render("dashboard", "userId", userId);
}
```

### Flash

Flash is a short-lived message that survives exactly one redirect. `FlashMiddleware` populates the injected `Flash<?>` from the _previous_ response's flash and clears it after the current request.

Set a flash value on the response to pass a message to the next request:

```java
public HttpResponse create(CustomerForm form) {
    if (!form.isValid()) {
        return builder(redirect("/customers/new"))
            .set(HttpResponse::setFlash, Flash.of("error", "Please fix validation errors."))
            .build();
    }
    // ... persist form ...
    return builder(redirect("/customers"))
        .set(HttpResponse::setFlash, Flash.of("notice", "Customer created successfully."))
        .build();
}
```

Read the flash in the next request's controller or template:

```java
public HttpResponse index(Flash<?> flash) {
    String notice = (String) flash.get("notice");
    return templateEngine.render("customers/index", "notice", notice);
}
```

## Return Values

A controller method can return:

- **`HttpResponse`** — the explicit response object. Use `HttpResponse.of("text")` for plain text or `redirect("/path")` for redirects.
- **A POJO** — serialized by `SerDesMiddleware` (e.g. to JSON) based on the `Accept` request header.
- **`String`** — treated as a template name by `RenderTemplateMiddleware` (Kotowari-specific).

```java
// Returns JSON when Accept: application/json
public List<Customer> list() {
    return daoProvider.get(CustomerDao.class).selectAll();
}

// Returns a rendered template
public HttpResponse show(Parameters params) {
    Customer customer = daoProvider.get(CustomerDao.class)
        .selectById(Long.valueOf(params.get("id")));
    return templateEngine.render("customers/show", "customer", customer);
}
```

## Validation

Kotowari supports validation of the request body object using JSR 303 Bean validation.

```java
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerForm extends FormBase {
    @NotBlank
    @Size(max = 10)
    private String name;

    @NotBlank
    private String password;

    @Email
    private String email;

    @Pattern(regexp = "[MF]")
    private String gender;

    private String birthday;
}
```
