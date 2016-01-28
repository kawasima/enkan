package enkan.component;

import javax.sql.DataSource;

/**
 * @author kawasima
 */
public abstract class DataSourceComponent extends SystemComponent {
    public abstract DataSource getDataSource();
}
