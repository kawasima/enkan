package enkan.component.jooq;

import enkan.component.ComponentLifecycle;
import enkan.component.ComponentRelationship;
import enkan.component.DataSourceComponent;
import enkan.system.EnkanSystem;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;

class JooqProviderTest {
    @Test
    void test() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;AUTOCOMMIT=FALSE;DB_CLOSE_DELAY=-1");

        EnkanSystem system = EnkanSystem.of("jooq", new JooqProvider(),
                "datasource", new TestDataSourceComponent(ds)).relationships(
                ComponentRelationship.component("jooq").using("datasource")
        );
        system.start();
        JooqProvider jooq = system.getComponent("jooq", JooqProvider.class);
        DSLContext ctx = jooq.getDSLContext();
        Result<Record> result = ctx.fetch("");
    }

    record Inquiry(String inquiryId) {

    }

    interface InquiryActivity {
        String activityId();
        LocalDateTime actAt();
    }

    record InquiryRegister(String activityId, LocalDateTime actAt) implements InquiryActivity {

    }

    static class User {
        private final String name;
        private final String email;
        private final Integer age;

        User(String name, String email, Integer age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public Integer getAge() {
            return age;
        }
    }

    static class UserMapper {
        User mapRecordToDomain(Record record) {
            return new User(record.get("name", String.class), record.get("email", String.class), record.get("age", Integer.class));
        }

        Record mapDomainToRecord(User user) {
            Table<Record> USERS_TABLE = DSL.table(DSL.name("users"));
            USERS_TABLE.fields(
                    DSL.field(DSL.name("name"), String.class)
            );
            return USERS_TABLE.newRecord();
        }
    }

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
                public void start(TestDataSourceComponent component) {
                }

                @Override
                public void stop(TestDataSourceComponent component) {
                }
            };
        }
    }

}