package enkan.middleware.jooq;

import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.component.ComponentLifecycle;
import enkan.component.ComponentRelationship;
import enkan.component.DataSourceComponent;
import enkan.component.jooq.JooqProvider;
import enkan.system.EnkanSystem;
import enkan.system.inject.ComponentInjector;
import enkan.util.Predicates;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class JooqDslContextMiddlewareTest {

    private EnkanSystem system;
    private JooqDslContextMiddleware<Object, Object> middleware;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:dsltest;DB_CLOSE_DELAY=-1");

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

        middleware = new JooqDslContextMiddleware<>();
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

    private MiddlewareChain<Object, Object, Object, Object> chain(Endpoint<Object, Object> endpoint) {
        return new DefaultMiddlewareChain<>(Predicates.any(), "test", endpoint);
    }

    @Test
    void dslContextIsSetOnExtendableRequest() {
        TestRequest request = new TestRequest();
        middleware.handle(request, chain(req -> {
            DSLContext dsl = ((TestRequest) req).getExtension("jooqDslContext");
            assertThat(dsl).isNotNull();
            return "ok";
        }));
    }

    @Test
    void dslContextCanExecuteQueries() {
        TestRequest request = new TestRequest();
        middleware.handle(request, chain(req -> {
            DSLContext dsl = ((TestRequest) req).getExtension("jooqDslContext");
            dsl.insertInto(table("users"))
                    .columns(field("id"), field("name"))
                    .values(1, "Alice")
                    .execute();
            return "ok";
        }));

        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();
        assertThat(ctx.select().from(table("users")).fetch()).hasSize(1);
    }

    @Test
    void nonExtendableRequestPassesThrough() {
        // A plain Object is not Extendable — should not throw, just pass through
        Object result = middleware.handle("plain", chain(req -> "ok"));
        assertThat(result).isEqualTo("ok");
    }

    // ------------------------------------------------------------------ helpers

    static class TestRequest implements enkan.data.Extendable {
        private final Map<String, Object> extensions = new HashMap<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getExtension(String name) { return (T) extensions.get(name); }

        @Override
        public <T> void setExtension(String name, T extension) { extensions.put(name, extension); }
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
