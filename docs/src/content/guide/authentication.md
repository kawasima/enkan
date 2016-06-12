type=page
status=published
title=Authentication and Authorization | Enkan
~~~~~~

# Authentication and Authorization

Enkan has the feature for authentication and authorization.

## Authentication

Enkan's authentication is to fetch the user principal from some authentication backends.
If fetching the user principal is successful, it is set to the request implements `PrincipalAvailable`.
Even if fetching the user principal fails, `AuthenticationMiddleware` does not any exception.
Because it is the role of authorization.

## Authorization

Currently, neither Enkan nor Kotowari provides the middleware for authorization.

Using a predicate of the middleware, you can make the authorization function.

```language-java
app.use(and(path("^/guestbook/"), authenticated().negate()),
        (Endpoint<HttpRequest, HttpResponse>) req ->
                redirect("/guestbook/login?url=" + req.getUri(), TEMPORARY_REDIRECT));
```
