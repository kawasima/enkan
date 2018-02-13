package enkan.component.eclipselink;

import enkan.component.DataSourceComponent;
import enkan.component.SystemComponent;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityManagerProvider<T extends SystemComponent> extends SystemComponent<T> {
    private String name;

    private DataSourceComponent dataSourceComponent;

    private Map<String, Object> jpaProperties = new HashMap<>();

    private EntityManagerFactory entityManagerFactory;
    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setJpaProperties(Map<String, Object> jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    protected String getName() {
        return this.name;
    }

    protected Map<String, Object> getJpaProperties() {
        return jpaProperties;
    }

    protected void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    protected DataSource getDataSource() {
        return dataSourceComponent.getDataSource();
    }

    protected void setDataSourceComponent(DataSourceComponent dataSourceComponent) {
        this.dataSourceComponent = dataSourceComponent;
    }
}
