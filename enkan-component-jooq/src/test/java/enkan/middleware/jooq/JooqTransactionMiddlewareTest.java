package enkan.middleware.jooq;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.component.ComponentLifecycle;
import enkan.component.ComponentRelationship;
import enkan.component.DataSourceComponent;
import enkan.component.jooq.JooqProvider;
import enkan.data.Routable;
import enkan.exception.MisconfigurationException;
import enkan.system.EnkanSystem;
import enkan.system.inject.ComponentInjector;
import enkan.util.Predicates;
import jakarta.transaction.Transactional;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static enkan.util.ReflectionUtils.tryReflection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class JooqTransactionMiddlewareTest {

    private EnkanSystem system;
    private JooqTransactionMiddleware<Object, Object> middleware;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:txtest;DB_CLOSE_DELAY=-1");

        JooqProvider jooq = new JooqProvider();
        jooq.setDialect(SQLDialect.H2);

        system = EnkanSystem.of(
                "jooq", jooq,
                "datasource", new TestDataSourceComponent(ds)
        ).relationships(
                ComponentRelationship.component("jooq").using("datasource")
        );
        system.start();

        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();
        ctx.execute("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(100))");

        middleware = new JooqTransactionMiddleware<>();
        Map<String, enkan.component.SystemComponent<?>> components = new HashMap<>();
        components.put("jooq", system.getComponent("jooq", JooqProvider.class));
        new ComponentInjector(components).inject(middleware);
    }

    @AfterEach
    void tearDown() {
        if (system == null) return;
        system.getComponent("jooq", JooqProvider.class).getDSLContext()
                .execute("DROP TABLE IF EXISTS users");
        system.stop();
    }

    private Object routableRequest(Method controllerMethod) {
        TestRequest req = new TestRequest();
        req.setControllerMethod(controllerMethod);
        return req;
    }

    private MiddlewareChain<Object, Object, Object, Object> chain(Endpoint<Object, Object> endpoint) {
        return new DefaultMiddlewareChain<>(Predicates.any(), "test", endpoint);
    }

    @Test
    void transactionalRequiredCommitsOnSuccess() {
        Object request = routableRequest(tryReflection(() -> TestController.class.getMethod("required")));
        middleware.handle(request, chain(req -> {
            DSLContext txCtx = ((TestRequest) req).getExtension("jooqDslContext");
            txCtx.insertInto(table("users"))
                    .columns(field("id"), field("name"))
                    .values(1, "Alice")
                    .execute();
            return "ok";
        }));

        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();
        assertThat(ctx.select().from(table("users")).fetch()).hasSize(1);
    }

    @Test
    void transactionalRequiredRollsBackOnException() {
        Object request = routableRequest(tryReflection(() -> TestController.class.getMethod("required")));
        assertThatThrownBy(() ->
            middleware.handle(request, chain(req -> {
                DSLContext txCtx = ((TestRequest) req).getExtension("jooqDslContext");
                txCtx.insertInto(table("users"))
                        .columns(field("id"), field("name"))
                        .values(1, "Alice")
                        .execute();
                throw new RuntimeException("forced rollback");
            }))
        ).isInstanceOf(RuntimeException.class);

        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();
        assertThat(ctx.select().from(table("users")).fetch()).isEmpty();
    }

    @Test
    void noAnnotationPassesThroughWithoutTransaction() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        Method m = tryReflection(() -> TestController.class.getMethod("noAnnotation"));
        middleware.handle(routableRequest(m), chain(req -> {
            ctx.insertInto(table("users"))
                    .columns(field("id"), field("name"))
                    .values(2, "Bob")
                    .execute();
            return "ok";
        }));

        Result<Record> result = ctx.select().from(table("users")).fetch();
        assertThat(result).hasSize(1);
    }

    @Test
    void unsupportedTxTypeThrowsMisconfigurationException() {
        Method m = tryReflection(() -> TestController.class.getMethod("mandatory"));
        assertThatThrownBy(() ->
            middleware.handle(routableRequest(m), chain(req -> "ok"))
        ).isInstanceOf(MisconfigurationException.class);
    }

    // ------------------------------------------------------------------ helpers

    static class TestRequest implements Routable {
        private Method controllerMethod;
        private final java.util.Map<String, Object> extensions = new java.util.HashMap<>();

        @Override
        public Method getControllerMethod() { return controllerMethod; }

        @Override
        public void setControllerMethod(Method method) { this.controllerMethod = method; }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getExtension(String name) { return (T) extensions.get(name); }

        @Override
        public <T> void setExtension(String name, T extension) { extensions.put(name, extension); }
    }

    static class TestController {
        @Transactional(Transactional.TxType.REQUIRED)
        public void required() {}

        @Transactional(Transactional.TxType.MANDATORY)
        public void mandatory() {}

        public void noAnnotation() {}
    }

    static class TestDataSourceComponent extends DataSourceComponent<TestDataSourceComponent> {
        private final DataSource ds;

        TestDataSourceComponent(DataSource ds) { this.ds = ds; }

        @Override
        public DataSource getDataSource() { return ds; }

        @Override
        protected ComponentLifecycle<TestDataSourceComponent> lifecycle() {
            return new ComponentLifecycle<>() {
                @Override public void start(TestDataSourceComponent c) {}
                @Override public void stop(TestDataSourceComponent c) {}
            };
        }
    }
}
