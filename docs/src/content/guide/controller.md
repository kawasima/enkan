type=page
status=published
~~~~~~

# Controllers

## Component Injection

## Arguments

Arguments of a controller method are available as follows:

- Parameters
- UserPrincipal
- Session
- Flash
- Conversation
- ConversationState
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

Parameters are a map represents a query string and a url encoded form.

### UserPrincipal


## Validation

Kotowari supports validation of the request body object using JSR 303 Bean validation.

```language-java
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