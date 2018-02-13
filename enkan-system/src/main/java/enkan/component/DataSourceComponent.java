package enkan.component;

import javax.sql.DataSource;

/**
 * Manages a data source.
 *
 * @author kawasima
 */
public abstract class DataSourceComponent<T extends DataSourceComponent> extends SystemComponent<T> {
    /**
     * Gets the data source that it holds.
     *
     * @return a DataSource
     */
    public abstract DataSource getDataSource();
}
