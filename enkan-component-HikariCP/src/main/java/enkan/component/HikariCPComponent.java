package enkan.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import enkan.collection.OptionMap;

import javax.sql.DataSource;

/**
 * @author kawasima
 */
public class HikariCPComponent extends DataSourceComponent {
    private HikariConfig config;
    private HikariDataSource dataSource;

    public HikariCPComponent(OptionMap options) {
        config = new HikariConfig();
        if (options.containsKey("uri")) config.setJdbcUrl(options.getString("uri"));
        if (options.containsKey("username")) config.setUsername(options.getString("username"));
        if (options.containsKey("password")) config.setPassword(options.getString("password"));
        if (options.containsKey("autoCommit?")) config.setAutoCommit(options.getBoolean("autoCommit?"));
        if (options.containsKey("connTimeout")) config.setConnectionTimeout(options.getLong("connTimeout"));
        if (options.containsKey("idleTimeout")) config.setIdleTimeout(options.getLong("idleTimeout"));
        if (options.containsKey("maxLifetime")) config.setMaxLifetime(options.getLong("maxLifetime"));
        if (options.containsKey("maxPoolSize")) config.setMaximumPoolSize(options.getInt("maxPoolSize"));
        if (options.containsKey("minIdle")) config.setMinimumIdle(options.getInt("minIdle"));
        if (options.containsKey("poolName")) config.setPoolName(options.getString("uri"));
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<HikariCPComponent>() {
            @Override
            public void start(HikariCPComponent component) {
                if (component.dataSource == null) {
                    component.dataSource = new HikariDataSource(config);
                }
            }

            @Override
            public void stop(HikariCPComponent component) {
                component.dataSource = null;
            }
        };
    }
}
