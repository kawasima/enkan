package enkan.component.jooq;

import enkan.component.ComponentLifecycle;
import enkan.component.ComponentRelationship;
import enkan.component.DataSourceComponent;
import enkan.exception.MisconfigurationException;
import enkan.system.EnkanSystem;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class JooqProviderTest {

    private EnkanSystem system;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        JooqProvider jooq = new JooqProvider();
        jooq.setDialect(SQLDialect.H2);

        system = EnkanSystem.of(
                "jooq", jooq,
                "datasource", new TestDataSourceComponent(ds)
        ).relationships(
                ComponentRelationship.component("jooq").using("datasource")
        );
        system.start();

        // Create table for tests
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();
        ctx.execute("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(200))");
    }

    @AfterEach
    void tearDown() {
        if (system == null) return;
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();
        ctx.execute("DROP TABLE IF EXISTS users");
        system.stop();
    }

    @Test
    void dslContextIsAvailableAfterStart() {
        JooqProvider jooq = system.getComponent("jooq", JooqProvider.class);
        assertThat(jooq.getDSLContext()).isNotNull();
    }

    @Test
    void insertAndSelectWork() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        ctx.insertInto(table("users"))
                .columns(field("id"), field("name"), field("email"))
                .values(1, "Alice", "alice@example.com")
                .execute();

        Result<Record> result = ctx.select()
                .from(table("users"))
                .where(field("id").eq(1))
                .fetch();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get(1, String.class)).isEqualTo("Alice");
        assertThat(result.get(0).get(2, String.class)).isEqualTo("alice@example.com");
    }

    @Test
    void multipleRowsCanBeInsertedAndFetched() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        ctx.insertInto(table("users"))
                .columns(field("id"), field("name"), field("email"))
                .values(1, "Alice", "alice@example.com")
                .values(2, "Bob", "bob@example.com")
                .execute();

        Result<Record> result = ctx.select().from(table("users")).fetch();
        assertThat(result).hasSize(2);
    }

    @Test
    void updateReflectsNewValue() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        ctx.insertInto(table("users"))
                .columns(field("id"), field("name"), field("email"))
                .values(1, "Alice", "alice@example.com")
                .execute();

        ctx.update(table("users"))
                .set(field("email"), "new@example.com")
                .where(field("id").eq(1))
                .execute();

        String email = ctx.select(field("email"))
                .from(table("users"))
                .where(field("id").eq(1))
                .fetchOne(field("email", String.class));

        assertThat(email).isEqualTo("new@example.com");
    }

    @Test
    void deleteRemovesRow() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        ctx.insertInto(table("users"))
                .columns(field("id"), field("name"), field("email"))
                .values(1, "Alice", "alice@example.com")
                .execute();

        ctx.deleteFrom(table("users"))
                .where(field("id").eq(1))
                .execute();

        Result<Record> result = ctx.select().from(table("users")).fetch();
        assertThat(result).isEmpty();
    }

    @Test
    void dslContextIsUnavailableAfterStop() {
        system.stop();
        JooqProvider jooq = system.getComponent("jooq", JooqProvider.class);

        assertThatThrownBy(jooq::getDSLContext)
                .isInstanceOf(MisconfigurationException.class);

        // Prevent double-stop in @AfterEach
        system = null;
    }

    @Test
    void dialectIsApplied() {
        JooqProvider jooq = system.getComponent("jooq", JooqProvider.class);
        DSLContext ctx = jooq.getDSLContext();
        assertThat(ctx.configuration().dialect()).isEqualTo(SQLDialect.H2);
    }

    // --------------------------------------------------------------- transaction

    @Test
    void transactionIsCommittedOnSuccess() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        ctx.transaction(config -> {
            DSLContext tx = DSL.using(config);
            tx.insertInto(table("users"))
                    .columns(field("id"), field("name"), field("email"))
                    .values(1, "Alice", "alice@example.com")
                    .execute();
            tx.insertInto(table("users"))
                    .columns(field("id"), field("name"), field("email"))
                    .values(2, "Bob", "bob@example.com")
                    .execute();
        });

        Result<Record> result = ctx.select().from(table("users")).fetch();
        assertThat(result).hasSize(2);
    }

    @Test
    void transactionIsRolledBackOnException() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        assertThatThrownBy(() ->
            ctx.transaction(config -> {
                DSLContext tx = DSL.using(config);
                tx.insertInto(table("users"))
                        .columns(field("id"), field("name"), field("email"))
                        .values(1, "Alice", "alice@example.com")
                        .execute();
                // 同一 id で重複インサート → 例外 → ロールバック
                tx.insertInto(table("users"))
                        .columns(field("id"), field("name"), field("email"))
                        .values(1, "Duplicate", "dup@example.com")
                        .execute();
            })
        ).isInstanceOf(Exception.class);

        Result<Record> result = ctx.select().from(table("users")).fetch();
        assertThat(result).isEmpty();
    }

    @Test
    void transactionResultReturnsValue() {
        DSLContext ctx = system.getComponent("jooq", JooqProvider.class).getDSLContext();

        int count = ctx.transactionResult(config -> {
            DSLContext tx = DSL.using(config);
            tx.insertInto(table("users"))
                    .columns(field("id"), field("name"), field("email"))
                    .values(1, "Alice", "alice@example.com")
                    .execute();
            return tx.fetchCount(table("users"));
        });

        assertThat(count).isEqualTo(1);
    }

    // ------------------------------------------------------------------ helpers

    static class TestDataSourceComponent extends DataSourceComponent<TestDataSourceComponent> {
        private final DataSource ds;

        TestDataSourceComponent(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public DataSource getDataSource() {
            return ds;
        }

        @Override
        protected ComponentLifecycle<TestDataSourceComponent> lifecycle() {
            return new ComponentLifecycle<>() {
                @Override
                public void start(TestDataSourceComponent component) {}

                @Override
                public void stop(TestDataSourceComponent component) {}
            };
        }
    }
}
