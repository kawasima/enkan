type=page
status=published
title=Authentication and Authorization | Enkan
~~~~~~

# Authentication and Authorization

---

## How Authentication Works

`AuthenticationMiddleware` runs a list of `AuthBackend` implementations in order.
Each backend gets a chance to parse the request and return a `Principal`.
The first non-null principal wins; remaining backends are skipped.

The resolved principal is added to the request via the `PrincipalAvailable` mixin:

```language-java
app.use(new AuthenticationMiddleware<>(List.of(
    new SessionBackend(),
    new MyTokenBackend()
)));
```

If **no** backend produces a principal, the middleware still passes the request downstream —
it does not throw or redirect. Enforcing access is the job of authorization (see below).

---

## Built-in Backends

### SessionBackend

Reads a `Principal` stored under the key `"principal"` in the session.
Use this for classical login-form authentication where the principal is stored after a
successful credential check.

```language-java
// On login success — store the principal in the session
HttpResponse response = builder(redirect("/dashboard"))
    .set(HttpResponse::setSession, Session.of("principal", new UserPrincipal(user)))
    .build();
```

```language-java
// Register the backend
app.use(new AuthenticationMiddleware<>(List.of(new SessionBackend())));
```

`SessionBackend` requires `SessionMiddleware` to run first (it reads from `request.getSession()`).

### TokenBackend

Parses a bearer token from the `Authorization` header:

```
Authorization: Token <your-token-here>
```

`TokenBackend.authenticate()` returns `null` by default — you must subclass it and implement
your own token verification:

```language-java
public class JwtTokenBackend extends TokenBackend {
    private final JwtVerifier verifier;

    public JwtTokenBackend(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Principal authenticate(HttpRequest request, String token) {
        return verifier.verify(token)   // returns null if invalid
                       .map(claims -> new JwtPrincipal(claims))
                       .orElse(null);
    }
}
```

To use a different header scheme (e.g. `Bearer`), call `setTokenName("Bearer")`.

---

## Implementing a Custom Backend

Implement `AuthBackend<REQ, T>`:

```language-java
public class BasicAuthBackend implements AuthBackend<HttpRequest, UsernamePassword> {

    private final UserRepository users;

    @Override
    public UsernamePassword parse(HttpRequest request) {
        String header = Objects.toString(request.getHeaders().get("Authorization"), "");
        if (!header.startsWith("Basic ")) return null;
        String decoded = new String(Base64.getDecoder().decode(header.substring(6)));
        String[] parts = decoded.split(":", 2);
        return parts.length == 2 ? new UsernamePassword(parts[0], parts[1]) : null;
    }

    @Override
    public Principal authenticate(HttpRequest request, UsernamePassword credentials) {
        return users.findByUsernameAndPassword(credentials.username(), credentials.password())
                    .map(UserPrincipal::new)
                    .orElse(null);
    }
}
```

`parse()` extracts the raw credential data from the request; returning `null` means "this
backend does not apply to this request" and the next backend is tried.

`authenticate()` validates the credential and returns a `Principal`, or `null` on failure.

---

## Reading the Principal in a Controller

When `AuthenticationMiddleware` has run, the request implements `PrincipalAvailable`.
Kotowari injects the principal as a method argument:

```language-java
public HttpResponse profile(UserPrincipal principal) {
    if (principal == null) {
        return redirect("/login");
    }
    return templateEngine.render("profile", "user", principal);
}
```

Or via the `@Inject`-based session approach for form-based login:

```language-java
public HttpResponse dashboard(UserPrincipal principal) {
    return templateEngine.render("dashboard", "principal", principal);
}
```

---

## Authorization

Enkan does not provide a dedicated authorization middleware.
Instead, use **predicates** to restrict access before the protected middleware even runs.

### Redirect unauthenticated users

```language-java
app.use(
    and(path("^/admin/"), authenticated().negate()),
    (Endpoint<HttpRequest, HttpResponse>) req ->
        redirect("/login?url=" + req.getUri(), TEMPORARY_REDIRECT)
);
```

### Restrict by permission

If your `Principal` implements a permission check, wrap it in a custom predicate:

```language-java
public class HasPermission implements Predicate<HttpRequest> {
    private final String permission;

    public HasPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean test(HttpRequest request) {
        PrincipalAvailable pa = (PrincipalAvailable) request;
        if (pa.getPrincipal() instanceof RoleBasedPrincipal rbp) {
            return rbp.hasPermission(permission);
        }
        return false;
    }
}
```

```language-java
app.use(
    and(path("^/admin/"), new HasPermission("ADMIN").negate()),
    (Endpoint<HttpRequest, HttpResponse>) req ->
        HttpResponse.of(403, "Forbidden")
);
```

### Summary

| Task | Tool |
|------|------|
| Identify who the user is | `AuthenticationMiddleware` + `AuthBackend` |
| Store login state | `session.put("principal", principal)` |
| Enforce access rules | Predicate + `Endpoint` redirect/403 |
| Read principal in handler | `UserPrincipal` method argument (Kotowari) |
