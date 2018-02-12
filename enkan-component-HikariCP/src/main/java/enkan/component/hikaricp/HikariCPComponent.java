package enkan.component.hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import enkan.collection.OptionMap;
import enkan.component.ComponentLifecycle;
import enkan.component.DataSourceComponent;

import javax.sql.DataSource;

/**
 * @author kawasima
 */
public class HikariCPComponent extends DataSourceComponent {
    private HikariConfig config;
    private HikariDataSource dataSource;

    public HikariCPComponent() {
        config = new HikariConfig();
    }

    public HikariCPComponent(OptionMap options) {
        this();
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
    protected ComponentLifecycle<HikariCPComponent> lifecycle() {
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

    public void setConfig(HikariConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "#HikariCPComponent {\n"
                + "  \"jdbcUrl\": \"" + config.getJdbcUrl() + "\",\n"
                + "  \"username\": \"" + config.getUsername() + "\",\n"
                + "  \"dependencies\": " + dependenciesToString()
                + "\n}";

    }
}
