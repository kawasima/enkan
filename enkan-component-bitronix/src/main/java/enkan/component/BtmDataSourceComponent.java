package enkan.component;

import bitronix.tm.resource.jdbc.PoolingDataSource;

import javax.sql.DataSource;

/**
 * @author kawasima
 */
public class BtmDataSourceComponent extends DataSourceComponent {
    @Override
    public DataSource getDataSource() {
        PoolingDataSource ds = new PoolingDataSource();
        ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        ds.init();
        return null;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return null;
    }
}
