package enkan.component.jooq;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import enkan.exception.FalteringEnvironmentException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JooqProvider extends SystemComponent<JooqProvider> {
    private DataSource dataSource;

    public DSLContext getDSLContext() {
        try {
            Connection connection = dataSource.getConnection();
            return DSL.using(connection);
        } catch (SQLException e) {
            throw new FalteringEnvironmentException(e);
        }
    }

    @Override
    protected ComponentLifecycle<JooqProvider> lifecycle() {
        return new ComponentLifecycle<>() {
            @Override
            public void start(JooqProvider component) {
                DataSourceComponent<?> dataSourceComponent = component.getDependency(DataSourceComponent.class);
                component.dataSource = dataSourceComponent.getDataSource();
            }

            @Override
            public void stop(JooqProvider component) {

            }
        };
    }
}
