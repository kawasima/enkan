type=page
status=published
title=Custom Middleware | Enkan
~~~~~~

# Custom Middleware

Writing your own middleware is the primary way to add cross-cutting behaviour to an Enkan application.
A middleware is a plain Java class — no base class to extend, no framework registration required.

---

## Choosing the Right Interface

| Interface | When to use |
|-----------|-------------|
| `DecoratorMiddleware<REQ, RES>` | The middleware decorates the request or response without changing their types (the common case). |
| `WebMiddleware` | Shorthand for `DecoratorMiddleware<HttpRequest, HttpResponse>`. Use this for web-specific middleware. |
| `Middleware<REQ, RES, NREQ, NRES>` | The middleware transforms the request or response into a different type. |

For most web middleware, implement `WebMiddleware`.

---

## A Minimal Example

```java
import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.WebMiddleware;

@Middleware(name = "requestId")
public class RequestIdMiddleware implements WebMiddleware {

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request,
            MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        // Before downstream
        String id = UUID.randomUUID().toString();
        request.setHeader("X-Request-Id", id);

        HttpResponse response = castToHttpResponse(chain.next(request));

        // After downstream
        if (response != null) {
            response.getHeaders().put("X-Request-Id", id);
        }
        return response;
    }
}
```

Register it in your `ApplicationFactory`:

```java
app.use(new RequestIdMiddleware());
```

---

## Declaring Dependencies

If your middleware requires another middleware to have run first, declare it with the `@Middleware` annotation.
Enkan validates the stack order at startup and throws a `MisconfigurationException` if the dependency is missing.

```java
@Middleware(name = "myMiddleware", dependencies = {"session", "cookies"})
public class MyMiddleware implements WebMiddleware { ... }
```

---

## Middleware with Configuration

Add fields and setters for configuration. Middleware instances are singletons, so fields are
set once before the application starts.

```java
@Middleware(name = "rateLimit")
public class RateLimitMiddleware implements WebMiddleware {
    private int requestsPerMinute = 60;

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request,
            MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        if (isOverLimit(request)) {
            return HttpResponse.of(429, "Too Many Requests");
        }
        return castToHttpResponse(chain.next(request));
    }
}
```

```java
RateLimitMiddleware rl = new RateLimitMiddleware();
rl.setRequestsPerMinute(100);
app.use(rl);
```

---

## Middleware with Component Injection

If the middleware needs a component (e.g. a database or cache), declare it with `@Inject`.
The `EnkanSystem` injects it before the application starts.

```java
@Middleware(name = "apiKeyAuth")
public class ApiKeyAuthMiddleware implements WebMiddleware {

    @Inject
    private ApiKeyRepository repository;  // SystemComponent injected automatically

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request,
            MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        String key = Objects.toString(request.getHeaders().get("X-Api-Key"), "");
        if (!repository.isValid(key)) {
            return HttpResponse.of(401, "Unauthorized");
        }
        return castToHttpResponse(chain.next(request));
    }
}
```

---

## Adding Capabilities with Mixin

To attach new data to the request without modifying the `HttpRequest` interface,
use `MixinUtils.mixin()`. Define a marker interface backed by the `Extendable` property bag:

```java
public interface TenantAvailable {
    default String getTenantId() {
        return (String) ((Extendable) this).getExtensions().get("tenantId");
    }
    default void setTenantId(String tenantId) {
        ((Extendable) this).getExtensions().put("tenantId", tenantId);
    }
}
```

```java
@Middleware(name = "tenant")
public class TenantMiddleware implements WebMiddleware {

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request,
            MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        request = MixinUtils.mixin(request, TenantAvailable.class);
        String tenantId = resolveTenantId(request);
        ((TenantAvailable) request).setTenantId(tenantId);
        return castToHttpResponse(chain.next(request));
    }
}
```

Downstream handlers can then cast the request to `TenantAvailable` to read the tenant ID.

---

## Applying Middleware Conditionally

Use a predicate to apply middleware only to matching requests:

```java
// Only on /api/* paths
app.use(path("^/api/"), new ApiKeyAuthMiddleware());

// Only on authenticated requests
app.use(authenticated(), new AuditLogMiddleware());

// Compose predicates
app.use(and(path("^/admin/"), authenticated()), new AdminMiddleware());
```

---

## Testing Custom Middleware

See the [Testing guide](testing.md) for the standard pattern using `DefaultMiddlewareChain` and a stub endpoint.
