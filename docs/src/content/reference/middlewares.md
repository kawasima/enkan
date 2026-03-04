type=page
status=published
title=Middleware catalog | Enkan
~~~~~~

# Middleware catalog

## Core

enkan-core package has middlewares as follows:

### Authentication

AuthenticationMiddleware enables to authenticate a request using some backends.

#### Properties

|Name|Description|
|:-----|:----|
|backends|The list of authentication backends.|

#### Usage

```language-java
app.use(new AuthenticationMiddleware(backends));
```

### ServiceUnavailable

#### Usage

```language-java
app.use(new ServiceUnavailableMiddleware(serviceUnavailableEndpoint));
```
#### Properties

|Name    |Type    |Description|
|:-------|:-------|:-----------|
|endpoint|Endpoint|Returns the response when service is unavailable.|
## Web

enkan-web package has middlewares as follows:

### AbsoluteRedirects

`AbsoluteRedirectsMiddleware` rewrites the URL contains `Location` http response header to the absolute URL.

```
Location: /abc/123
    ↓
Location: http://myhost/abc/123
```

#### Usage

```language-java
app.use(new AbsoluteRedirectsMiddleware());
```

#### Properties

AbsoluteRedirectsMiddleware has no properties.

### AntiForgery

`AntiForgeryMiddleware` appends the token check for protecting from the CSRF attack.
When the request method isn't GET/HEAD/OPTIONS, AntiForgeryMiddleware validates the given token.
If the token is invalid, it returns the forbidden response.

Because `AntiForgeryMiddleware` requires Session, We recommend using `Conversation` instead of this.

#### Usage

```language-java
app.use(new AntiForgeryMiddleware());
```

#### Properties

AntiForgeryMiddleware has no properties.

### ContentNegotiation

Parses the `Accept` header and sets the value to a request.

#### Usage

```language-java
app.use(new ContentNegotiationMiddleware());
```

### ContentType

Adds the `Content-Type` header if the response does not contains a `Content-Type` header yet.

#### Usage

```language-java
app.use(new ContentTypeMiddleware());
```

### Conversation

Creates a conversation and save states related with its conversation to the conversation store.

#### Usage

```language-java
app.use(new ConversationMiddleware());
```

### Cookies

Parses `Cookie` header and sets the `Set-Cookie` header to the response.

#### Usage

```language-java
app.use(new CookiesMiddleware());
```

### DefaultCharset

Adds the default charset to the response.

#### Usage

```language-java
app.use(new DefaultCharsetMiddleware());
```

### Flash

Serializes and deserializes a flash value.

#### Usage

```language-java
app.use(new FlashMiddleware());
```

### IdleSessionTimeout

Enkan Session has no expires in default. This middleware append the feature of timeout to the session.

#### Usage

```language-java
app.use(new IdleSessionTimeoutMiddleware());
```

### MethodOverride

Override a request method.

#### Usage

```language-java
app.use(new MethodOverrideMiddleware());
```

### MultipartParams

Parses the multipart request.

#### Usage

```language-java
app.use(new MultipartParamsMiddleware());
```

### NestedParams

NestedParamsMiddleware enables to parse nested parameters like `foo[][bar]`. It requires ParamMiddleware.

#### Usage

```language-java
app.use(new NestedParamsMiddleware());
```

### Normalization

Normalize the parameter values. It's for trimming spaces or converting letter case.

#### Usage

```language-java
app.use(new NormalizationMiddleware());
```

### Params

ParamMiddleware enables to parse urlencoded query string and post body and set to a request object. 

#### Usage

```language-java
app.use(new ParamsMiddleware());
```

### Resources

Returns the asset file that is searched from classpath.

#### Usage

```language-java
app.use(new ResourceMiddleware());
```

#### Properties

|Name|Description|
|:-----|:----|
|rootPath|The path prefix. Default value is `public`|



### Session

SessionMiddleware enables to store/load session objects to/from in-memory stores.

#### Usage

```language-java
app.use(new SessionMiddleware());
```

#### Properties

|Name|Description|
|:-----|:----|
|cookieName|A name of cookie for session id. Default value is `enkan-session`|
|store|A storage for session.|


### Trace

Adds the response header for tracing using middlewares.

#### Usage

```language-java
app.use(new TraceMiddleware());
```

### SecurityHeaders

Applies a suite of security-related HTTP response headers in a single middleware (similar to [Helmet.js](https://helmetjs.github.io/) for Express).

All headers are enabled by default with safe values. Pass `null` to any setter to disable a specific header.

#### Default headers

| Header | Default value |
|:---|:---|
| `Content-Security-Policy` | `default-src 'self'` |
| `Strict-Transport-Security` | `max-age=15552000; includeSubDomains` |
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `SAMEORIGIN` |
| `X-XSS-Protection` | `0` (disabled — use CSP instead) |
| `Referrer-Policy` | `strict-origin-when-cross-origin` |
| `Cross-Origin-Opener-Policy` | `same-origin` |
| `Cross-Origin-Resource-Policy` | `same-origin` |

#### Usage

```language-java
// All defaults
app.use(new SecurityHeadersMiddleware());

// Custom CSP, disable HSTS for local development
SecurityHeadersMiddleware sec = new SecurityHeadersMiddleware();
sec.setContentSecurityPolicy("default-src 'self'; img-src *");
sec.setStrictTransportSecurity(null); // disable
app.use(sec);
```

#### Properties

| Name | Description |
|:---|:---|
| `contentSecurityPolicy` | `Content-Security-Policy` value. `null` disables the header. |
| `strictTransportSecurity` | `Strict-Transport-Security` value. `null` disables the header. |
| `contentTypeOptions` | `X-Content-Type-Options` value. Default: `nosniff`. |
| `frameOptions` | `X-Frame-Options` value. Default: `SAMEORIGIN`. |
| `xssProtection` | `X-XSS-Protection` value. Default: `0`. |
| `referrerPolicy` | `Referrer-Policy` value. |
| `crossOriginOpenerPolicy` | `Cross-Origin-Opener-Policy` value. |
| `crossOriginResourcePolicy` | `Cross-Origin-Resource-Policy` value. |

## Kotowari

### ControllerInvoker

Invoke a controller method.

#### Usage

```language-java
app.use(new ControllerInvokerMiddleware());
```

### Form

Populate the request body to a Java bean object.

#### Usage

```language-java
app.use(new FormMiddleware());
```

#### Properties

`FormMiddleware` has no properties.

### RenderTemplate

Render HTML using a template. When a controller returns `TemplatedHttpResponse`, it has yet to be rendered.  

#### Usage

```language-java
app.use(new RenderTemplateMiddleware());
```

#### Properties

`RenderTemplateMiddleware` has no properties.

### Routing

Routes the request to a controller method.

#### Usage

```language-java
app.use(new RoutingMiddleware(routes));
```
#### Properties

|Name|Description|
|:-----|:----|
|routes|Defined routes|

### SerDes

Deserialize the request body and serializes the response body.

#### Usage

```language-java
app.use(new SerDesMiddleware());
```

#### Properties

|Name|Description|
|:-----|:----|
|bodyReaders||
|bodyWriters||
 

### Transaction

Manages database transactions around controller invocation.

#### Usage

```language-java
// requires enkan-component-doma2 dependency
app.use(new DomaTransactionMiddleware<>(config));
```

### ValidateBody

Validates the body object. If body object implements the `Validatable` interface, the error messages are set to it.

#### Usage

```language-java
app.use(new ValidateBodyMiddleware());
```

#### Properties

`ValidateBodyMiddleware` has no properties.

## Additional Middleware

### Cors

`CorsMiddleware` handles Cross-Origin Resource Sharing (CORS) headers.

#### Usage

```language-java
app.use(new CorsMiddleware());
```

### ForwardedScheme

`ForwardedSchemeMiddleware` detects the original request scheme from `X-Forwarded-Proto` header and sets it on the request.

#### Usage

```language-java
app.use(new ForwardedSchemeMiddleware());
```

