package enkan.component.jpa;

import enkan.collection.OptionMap;
import enkan.component.DataSourceComponent;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;

import java.sql.Connection;
import java.sql.Statement;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

public class EntityManagerProviderTest {
    @Test
    public void test() throws Exception {
        EnkanSystem system = EnkanSystem.of(
                "jpa", builder(new EntityManagerProvider())
                        .set(EntityManagerProvider::setName, "test")
                        .set(EntityManagerProvider::registerClass, Person.class)
                        .build(),
                "datasource", new HikariCPComponent(OptionMap.of(
                        "uri", "jdbc:h2:mem:test;AUTOCOMMIT=FALSE;DB_CLOSE_DELAY=-1"
                ))
        ).relationships(
                component("jpa").using("datasource")
        );
        system.start();
        DataSourceComponent dsComponent = system.getComponent("datasource");
        try (Connection connection = dsComponent.getDataSource().getConnection();
             Statement stmt = connection.createStatement()){
            stmt.executeUpdate("CREATE TABLE person(id IDENTITY, name VARCHAR(100))");
        }
        try {
            EntityManagerProvider provider = system.getComponent("jpa");
            EntityManager em = provider.getEntityManager();
            Person person = em.find(Person.class, 1L);
            assertThat(person).isNull();
            person = new Person();
            person.setName("hoho");
            em.getTransaction().begin();
            em.merge(person);
            em.getTransaction().commit();

            Person person2 = em.find(Person.class, 1L);
            System.out.println(person2);
        } finally {
            system.stop();
        }
    }
}
