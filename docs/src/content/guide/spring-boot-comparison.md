type=page
status=published
title=Spring Boot Comparison | Enkan
~~~~~~

# Spring Boot → Enkan Comparison

This guide shows how common Spring Boot patterns map to Enkan/Kotowari equivalents.
It is not a feature-by-feature comparison — it highlights the **difference in philosophy**.

---

## 1. Dependency Injection / Component Wiring

### Spring Boot

```language-java
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig);
    }
}

@Service
public class UserService {
    @Autowired
    private DataSource dataSource;
}
```

### Enkan

```language-java
EnkanSystem.of(
    "datasource", new HikariCPComponent(),
    "doma",       new DomaProvider(),
    "app",        new ApplicationComponent("com.example.MyAppFactory"),
    "http",       new UndertowComponent()
).relationships(
    component("http").using("app"),
    component("app").using("doma", "datasource"),
    component("doma").using("datasource")
);
```

**Key difference:** No classpath scanning, no `@Component` discovery. The dependency graph is a single block of Java code — you can read the entire wiring at a glance. Components are started in dependency order and stopped in reverse.

---

## 2. Request Pipeline

### Spring Boot

```language-java
@Component
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        // before
        chain.doFilter(req, res);
        // after
    }
}
```

Filter ordering is controlled by `@Order` or `FilterRegistrationBean`.

### Enkan

```language-java
app.use(new DefaultCharsetMiddleware());
app.use(new MetricsMiddleware<>());
app.use(new SessionMiddleware());
app.use(new RoutingMiddleware());
```

**Key difference:** Middleware ordering is explicit — the order you write `app.use()` is the order they execute. No annotation-driven ordering surprises. Middleware can also declare `@Middleware(dependencies = {...})` so the framework rejects invalid orderings at startup.

---

## 3. Routing

### Spring Boot

```language-java
@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping
    public List<User> list() { ... }

    @GetMapping("/{id}")
    public User show(@PathVariable Long id) { ... }

    @PostMapping
    public User create(@RequestBody User user) { ... }
}
```

### Enkan (Kotowari)

```language-java
Routes routes = Routes.define(r -> {
    r.get("/").to(HomeController.class, "index");
    r.resource(UserController.class);
    r.scope("/admin", admin -> {
        admin.resource(AdminUserController.class);
    });
}).compile();
```

The controller is a plain class — no annotations on methods:

```language-java
public class UserController {
    @Inject
    private DomaProvider daoProvider;

    public HttpResponse index() {
        UserDao dao = daoProvider.getDao(UserDao.class);
        return templateEngine.render("user/index", "users", dao.findAll());
    }

    public HttpResponse show(Parameters params) {
        Long id = params.getLong("id");
        // ...
    }
}
```

**Key difference:** Routes are defined centrally in Java code, not scattered across controller annotations. `r.resource(Controller.class)` generates RESTful routes (index, show, newForm, create, edit, update, delete) similar to Rails.

---

## 4. Configuration

### Spring Boot

```language-yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost/mydb
```

```language-java
@Value("${server.port}")
private int port;
```

### Enkan

```language-java
public class MySystemFactory implements SystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
            "datasource", new HikariCPComponent(),
            "http",       new UndertowComponent()
        );
    }
}
```

Component properties are set via Java setters or constructor arguments.

**Key difference:** No YAML, no `.properties`, no `@Value`. Everything is Java code — your IDE can refactor, navigate, and statically analyse the entire configuration. There is no runtime string-key lookup that can fail silently.

---

## 5. Session

### Spring Boot

```language-java
@GetMapping("/dashboard")
public String dashboard(HttpSession session) {
    session.setAttribute("visited", true);
    return "dashboard";
}
```

### Enkan

```language-java
public HttpResponse dashboard(Session session) {
    session.put("visited", true);
    return builder(templateEngine.render("dashboard"))
        .set(HttpResponse::setSession, session)
        .build();
}
```

**Key difference:** In Enkan, session changes must be explicitly attached to the response. Mutating the request-side session object alone does **not** persist changes. This makes session writes visible and intentional.

Also, `SessionMiddleware` must be explicitly registered — `getSession()` does not exist on the request until it runs.

---

## 6. Authentication & Authorization

### Spring Boot (Spring Security)

```language-java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        ).formLogin(withDefaults());
        return http.build();
    }
}
```

### Enkan

```language-java
// Authentication — register backends
app.use(new AuthenticationMiddleware<>(List.of(
    new SessionBackend(),
    new MyTokenBackend()
)));

// Authorization — use predicates
app.use(
    and(path("^/admin/"), authenticated().negate()),
    (Endpoint<HttpRequest, HttpResponse>) req ->
        redirect("/login", TEMPORARY_REDIRECT)
);
```

**Key difference:** Enkan separates authentication (who are you?) from authorization (can you access this?) using different mechanisms. Authentication is middleware with pluggable backends. Authorization is just predicates — the same predicate system used for all middleware conditions. There is no "security filter chain" abstraction.

---

## 7. Validation

### Spring Boot

```language-java
@PostMapping("/users")
public String create(@Valid @ModelAttribute UserForm form,
                     BindingResult result) {
    if (result.hasErrors()) {
        return "user/form";
    }
    // ...
}
```

### Enkan (Kotowari)

```language-java
// Middleware stack
app.use(new ValidateBodyMiddleware());

// Form class with JSR 303 annotations
public class CustomerForm extends FormBase {
    @NotBlank
    @Size(max = 10)
    private String name;

    @Email
    private String email;
}

// Controller — validation errors are available via the form
public HttpResponse create(CustomerForm form) {
    if (form.hasErrors()) {
        return templateEngine.render("customer/new", "customer", form);
    }
    // ...
}
```

**Key difference:** Validation is a middleware concern, not an annotation on the controller method. `ValidateBodyMiddleware` runs JSR 303 validation before the controller is invoked. The form object carries both data and errors.

---

## 8. Template Rendering

### Spring Boot

```language-java
@GetMapping("/users")
public String list(Model model) {
    model.addAttribute("users", userService.findAll());
    return "user/list";  // resolves to templates/user/list.html
}
```

### Enkan (Kotowari)

```language-java
@Inject
private TemplateEngine templateEngine;

public HttpResponse list() {
    List<User> users = dao.findAll();
    return templateEngine.render("user/list", "users", users);
}
```

**Key difference:** The controller returns an `HttpResponse` directly, not a view name string. There is no implicit model — template variables are passed explicitly. The template engine (Freemarker, Thymeleaf, etc.) is injected as a component.

---

## 9. Database / Transactions

### Spring Boot

```language-java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createUser(User user) {
        userRepository.save(user);
    }
}
```

### Enkan (with Doma)

```language-java
// Middleware — wraps request in a transaction
app.use(new DomaTransactionMiddleware<>());

// Controller
@Inject
private DomaProvider daoProvider;

public HttpResponse create(CustomerForm form) {
    CustomerDao dao = daoProvider.getDao(CustomerDao.class);
    dao.insert(form.toEntity());
    return redirect("/customers");
}
```

**Key difference:** Transactions are middleware, not annotations on service methods. The entire request is wrapped in a transaction by `DomaTransactionMiddleware`. There is no `@Transactional` — the middleware stack makes it clear which requests are transactional.

---

## 10. Static Resources

### Spring Boot

Files in `src/main/resources/static/` are automatically served at `/`.

### Enkan

```language-java
app.use(new ResourceMiddleware());
```

Serves files from `src/main/resources/public/` when registered. If you don't register `ResourceMiddleware`, no static files are served.

**Key difference:** Nothing is automatic. Static file serving is opt-in middleware.

---

## 11. Development

### Spring Boot (DevTools)

- `spring-boot-devtools` on classpath enables auto-restart
- Full JVM restart on class changes (~2-5 seconds)
- LiveReload for static resources

### Enkan (REPL)

```
enkan> /reset              # hot-reload application (~1 second)
enkan> /middleware app list # inspect middleware stack
enkan> /routes app         # inspect routing table
```

**Key difference:** Enkan reloads only the application layer, not the JVM. Database pools, template engines, and other long-initialisation components stay alive. The REPL also lets you inspect and modify the running system (toggle middleware predicates, check routes, etc.) — not just restart it.

---

## 12. Testing

### Spring Boot

```language-java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listUsers() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/list"));
    }
}
```

### Enkan

```language-java
class UserControllerTest {
    @Test
    void listUsers() {
        UserController controller = new UserController();
        // inject mock dependencies directly
        controller.templateEngine = new MockTemplateEngine();
        controller.daoProvider = new MockDomaProvider();

        HttpResponse response = controller.list();
        assertThat(response).isNotNull();
    }
}
```

**Key difference:** Controllers are plain classes — no container bootstrap needed. You instantiate them directly, inject mock dependencies, and call methods. No `@SpringBootTest`, no `MockMvc`, no application context startup. For integration tests, you can compose a minimal middleware stack and send requests through it.

---

## Summary

| Concern | Spring Boot | Enkan |
|---------|------------|-------|
| Philosophy | Convention over configuration | Explicitness over magic |
| Configuration | YAML + annotations | Java code only |
| DI | `@Autowired`, classpath scanning | `EnkanSystem.of()` + `relationships()` |
| Request pipeline | `Filter` + `@Order` | `app.use()` in order |
| Routing | `@GetMapping` on methods | `Routes.define()` centrally |
| Session | `HttpSession` (mutable, implicit) | Response-side session (explicit) |
| Auth | Spring Security filter chain | `AuthBackend` + predicates |
| Transactions | `@Transactional` | `DomaTransactionMiddleware` |
| Dev reload | JVM restart (~2-5s) | Application reload (~1s) |
| Testing | `@SpringBootTest` + `MockMvc` | Plain class instantiation |
