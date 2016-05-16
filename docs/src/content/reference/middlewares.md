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

### ContentType

### ContentTypeOptions

### Conversation

### Cookies

### DefaultCharset

### Flash

### FrameOptions

### IdleSessionTimeout

Enkan Session has no expires in default. This middleware append the feature of timeout to the session.

### MethodOverride

### MultipartParams

### NestedParams

NestedParamsMiddleware enables to parse nested parameters like `foo[][bar]`. It requires ParamMiddleware.

### Normalization

### Params

ParamMiddleware enables to parse urlencoded query string and post body and set to a request object. 

#### Usage

```language-java
app.use(new ParamsMiddleware());
```

### Resources

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

### XssProtection

## Kotowari

### ControllerInvoker

### Form

### RenderTemplate

### Routing

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

### ValidateForm


