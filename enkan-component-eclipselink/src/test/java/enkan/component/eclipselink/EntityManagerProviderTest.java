package enkan.component.eclipselink;

import enkan.collection.OptionMap;
import enkan.component.DataSourceComponent;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.component.jpa.EntityManagerProvider;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Connection;
import java.sql.Statement;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

public class EntityManagerProviderTest {
    @Test
    public void test() throws Exception {
        EnkanSystem system = EnkanSystem.of(
                "eclipselink", builder(new EclipseLinkEntityManagerProvider())
                        .set(EclipseLinkEntityManagerProvider::setName, "test")
                        .set(EclipseLinkEntityManagerProvider::registerClass, Person.class)
                        .build(),
                "datasource", new HikariCPComponent(OptionMap.of(
                        "uri", "jdbc:h2:mem:test;AUTOCOMMIT=FALSE;DB_CLOSE_DELAY=-1"
                ))
        ).relationships(
                component("eclipselink").using("datasource")
        );
        system.start();
        DataSourceComponent dsComponent = system.getComponent("datasource");
        try (Connection connection = dsComponent.getDataSource().getConnection();
             Statement stmt = connection.createStatement()){
            stmt.executeUpdate("CREATE TABLE person(id IDENTITY, name VARCHAR(100))");
        }
        try {
            EntityManagerProvider provider = system.getComponent("eclipselink");
            EntityManager em = provider.createEntityManager();
            Person person = em.find(Person.class, 1L);
            assertThat(person).isNull();
            person = new Person();
            person.setName("hoho");
            em.getTransaction().begin();
            em.merge(person);
            em.getTransaction().commit();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Person> query = cb.createQuery(Person.class);
            Root<Person> personRoot = query.from(Person.class);
            query.where(cb.equal(personRoot.get("id"), 1L));
            Person person2 = em.createQuery(query)
                    .getSingleResult();
            assertThat(person2).extracting("id", "name")
                    .containsExactly(1L, "hoho");
        } finally {
            system.stop();
        }
    }
}
