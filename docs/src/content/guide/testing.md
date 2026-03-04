type=page
status=published
title=Testing | Enkan
~~~~~~

# Testing

Enkan's design makes testing straightforward: middleware and components are plain Java objects with no hidden wiring.
You do not need a running HTTP server or a full application context to test most code.

---

## Testing Middleware in Isolation

The standard pattern is to construct a `DefaultMiddlewareChain` with a stub endpoint, then call `handle()` directly.

```language-java
import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.predicate.AnyPredicate;

class MyMiddlewareTest {

    private HttpResponse handleWith(MyMiddleware middleware, HttpResponse downstream) {
        MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain =
            new DefaultMiddlewareChain<>(
                new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, HttpResponse>) req -> downstream);
        return middleware.handle(new DefaultHttpRequest(), chain);
    }

    @Test
    void addsExpectedHeader() {
        MyMiddleware middleware = new MyMiddleware();
        HttpResponse response = HttpResponse.of("body");

        HttpResponse result = handleWith(middleware, response);

        assertThat(HttpResponseUtils.getHeader(result, "X-My-Header")).isEqualTo("expected");
    }
}
```

Key points:

- `DefaultHttpRequest` is a plain implementation of `HttpRequest` — set fields directly via setters or `BeanBuilder`.
- The `Endpoint` lambda acts as the downstream "handler" that returns the stubbed response.
- `AnyPredicate` ensures the middleware is always invoked regardless of predicate logic.

### Building request objects

Use `BeanBuilder` for concise request/response construction:

```language-java
import static enkan.util.BeanBuilder.builder;

HttpRequest request = builder(new DefaultHttpRequest())
    .set(HttpRequest::setRequestMethod, "POST")
    .set(HttpRequest::setUri, "/api/items")
    .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
    .build();
```

### Testing middleware that uses the Mixin pattern

Some middleware adds capabilities to the request via `MixinUtils.mixin()`.
After calling `handle()`, cast the request inside the endpoint lambda to the mixin interface:

```language-java
@Test
void setsLocaleOnRequest() {
    ContentNegotiationMiddleware cnm = new ContentNegotiationMiddleware();
    cnm.setAllowedLanguages(Set.of("en", "ja"));

    HttpRequest captured = new DefaultHttpRequest();
    captured.setHeaders(Headers.of("Accept-Language", "ja"));

    MiddlewareChain<HttpRequest, HttpResponse, ?, ?> chain =
        new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
            (Endpoint<HttpRequest, HttpResponse>) req -> {
                // req has ContentNegotiable mixin at this point
                assertThat(((ContentNegotiable) req).getLocale())
                    .isEqualTo(Locale.JAPANESE);
                return HttpResponse.of("");
            });

    cnm.handle(captured, chain);
}
```

---

## Testing Controllers

Controllers are plain Java classes. Instantiate them directly, inject component mocks via setters,
and call the method under test.

```language-java
class CustomerControllerTest {

    @Test
    void indexReturnsTemplatedResponse() {
        // Arrange
        CustomerController controller = new CustomerController();
        controller.setTemplateEngine(new FakeTemplateEngine());  // test double
        controller.setDaoProvider(new FakeDaoProvider());

        // Act
        HttpResponse response = controller.index(new CustomerForm());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
```

Because components are injected via `@Inject`-annotated fields (not constructor arguments),
you can set them in tests using a simple setter or direct field access:

```language-java
// Direct field access for tests (when no setter exists)
ReflectionTestUtils.setField(controller, "templateEngine", fakeEngine);
```

---

## Testing Components

Components are lifecycle objects. Test them by calling `start()` and `stop()` explicitly,
or just test the component's methods without the full lifecycle if the method does not require it.

```language-java
class HikariCPComponentTest {

    @Test
    void providesWorkingDataSource() throws Exception {
        HikariCPComponent component = new HikariCPComponent(
            OptionMap.of("uri", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"));
        // Use EnkanSystem for lifecycle management in integration tests
        EnkanSystem system = EnkanSystem.of("db", component);
        system.start();
        try {
            try (Connection conn = component.getDataSource().getConnection()) {
                assertThat(conn.isValid(1)).isTrue();
            }
        } finally {
            system.stop();
        }
    }
}
```

---

## Integration Testing

For integration tests that exercise the full middleware stack, build a minimal `Application`
and drive it with constructed requests — no HTTP server needed.

```language-java
class AppIntegrationTest {

    private Application<HttpRequest, HttpResponse> app;

    @BeforeEach
    void setUp() {
        app = new MyApplicationFactory().create(buildSystem());
    }

    @Test
    void getIndexReturns200() {
        HttpRequest request = builder(new DefaultHttpRequest())
            .set(HttpRequest::setRequestMethod, "GET")
            .set(HttpRequest::setUri, "/")
            .build();

        HttpResponse response = app.handle(request);

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
```

---

## Tips

- **Prefer `DefaultHttpRequest` over mocking** — it is a real implementation and avoids bytecode manipulation issues with newer JDKs.
- **Use `Headers.of(...)` for headers** — `Headers` is the correct type for both request and response headers.
- **Test one middleware at a time** — each middleware is a small, focused unit; integration tests should be reserved for verifying the composed stack.
