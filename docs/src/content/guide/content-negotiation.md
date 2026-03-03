type=page
status=published
title=Content negotiation | Enkan
~~~~~~

# Content negotiation

Enkan possible to serve different response at the same URI depending on the Accept header.
It enables to develop REST APIs.

`SerDesMiddleware` deserializes the request body and serializes the response body.
Available formats of the serialization are dependent on JAX-RS Entity providers.

If you add the dependency of `jackson-jaxrs-json-provider` into your Maven pom file, it will be enabled to serialize/deserialize JSON format.

```language-xml
<dependency>
    <groupId>com.fasterxml.jackson.jaxrs</groupId>
    <artifactId>jackson-jaxrs-json-provider</artifactId>
</dependency>
```

## Resource controller

If you want to return a response other than HTML format, simply return the Serializable object.

```language-java
public List<Customer> list() {
    CustomerDao customerDao = daoProvider.getDao(CustomerDao.class);
    return customerDao.selectAll();
}
```

You don't need any annotations! `SerDesMiddleware` converts the Java object to the appropriate format.

## Internationalization

`ContentNegotiationMiddleware` decides an optimal language for each request by reading the `Accept-Language` header and comparing it against the set of languages your application supports.

Register the middleware with your allowed languages:

```language-java
ContentNegotiationMiddleware cnm = new ContentNegotiationMiddleware();
cnm.setAllowedTypes(Set.of("text/html", "application/json"));
cnm.setAllowedLanguages(Set.of("en", "ja", "fr"));
app.use(cnm);
```

The middleware adds the `ContentNegotiable` mixin to the request. Downstream middleware and controllers can read the resolved locale:

```language-java
public HttpResponse index(Locale locale) {
    // locale is resolved from Accept-Language — e.g. Locale.JAPANESE
    return templateEngine.render("index", "locale", locale);
}
```

### Language matching rules

Matching follows RFC 7231 quality weighting (`q` values). A request with:

```
Accept-Language: ja;q=1.0, en;q=0.8, *;q=0.1
```

...will resolve to `ja` if it is in `allowedLanguages`, otherwise fall back to `en`, and so on. The wildcard `*` in `allowedLanguages` (the default) means any language is accepted as-is.

### Allowed content types

`setAllowedTypes` controls which media types the application can produce. The middleware picks the best match from the request's `Accept` header:

```language-java
cnm.setAllowedTypes(Set.of("text/html", "application/json", "application/xml"));
```

The resolved `MediaType` is available on the request via `ContentNegotiable#getMediaType()` and is used by `SerDesMiddleware` to choose the serialization format.
