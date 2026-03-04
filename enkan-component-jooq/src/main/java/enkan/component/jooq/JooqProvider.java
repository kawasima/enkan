package enkan.component.jooq;

import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

public class JooqProvider extends SystemComponent<JooqProvider> {
    private DataSource dataSource;
    private SQLDialect dialect = SQLDialect.DEFAULT;

    public DSLContext getDSLContext() {
        if (dataSource == null) {
            throw new MisconfigurationException("core.COMPONENT_NOT_FOUND", "DataSource", "JooqProvider");
        }
        return DSL.using(dataSource, dialect);
    }

    public void setDialect(SQLDialect dialect) {
        this.dialect = dialect;
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
                component.dataSource = null;
            }
        };
    }
}
