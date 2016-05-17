type=page
status=published
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
app.use(new AuthenticateMiddleware(backends));
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

### ContentTypeOptions

Adds the `X-Content-Type-Options` for protecting a XSS attack. 

#### Usage

```language-java
app.use(new ContentTypeOptionsMiddleware());
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

### FrameOptions

Adds the `X-Frame-Options` header to a response for a click jacking attack.

#### Usage

```language-java
app.use(new FrameOptionsMiddleware());
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
app.use(new ResourcesMiddleware());
```

#### Properties

|Name|Description|
|:-----|:----|
|rootPath|The path prefix. Default value is `public`|



### Session

SessionMiddleware enables to store/load session objects to/from in-memory or JCache.

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

### XssProtection

Adds `X-XSS-Protection` header to the response.

#### Usage

```language-java
app.use(new XssProtectionMiddleware());
```

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

TBD

### ValidateForm

Validates the body object. If body object is implemented the `Validatable` interface, the error messages is set to it.

#### Usage

```language-java
app.use(new ValidateFormMiddleware());
```

#### Properties

`ValidateFormMiddleware` has no properties. 
