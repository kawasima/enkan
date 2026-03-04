package enkan.component.eclipselink;

import enkan.collection.OptionMap;
import enkan.component.DataSourceComponent;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.component.jpa.EntityManagerProvider;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.sql.Connection;
import java.sql.Statement;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;
import static org.assertj.core.api.Assertions.assertThat;

public class EntityManagerProviderTest {
    private EnkanSystem system;

    @BeforeEach
    void setUp() throws Exception {
        system = EnkanSystem.of(
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

        DataSourceComponent<HikariCPComponent> dsComponent = system.getComponent("datasource");
        try (Connection connection = dsComponent.getDataSource().getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS person(id IDENTITY, name VARCHAR(100))");
        }
    }

    @AfterEach
    void tearDown() {
        if (system != null) {
            system.stop();
        }
    }

    @Test
    void persistAndFindEntity() {
        EntityManagerProvider<EclipseLinkEntityManagerProvider> provider = system.getComponent("eclipselink");
        try (EntityManager em = provider.createEntityManager()) {
            assertThat(em.find(Person.class, 1L)).isNull();

            Person person = new Person();
            person.setId(1L);
            person.setName("hoho");
            em.getTransaction().begin();
            em.merge(person);
            em.getTransaction().commit();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Person> query = cb.createQuery(Person.class);
            Root<Person> personRoot = query.from(Person.class);
            query.where(cb.equal(personRoot.get("id"), 1L));
            Person found = em.createQuery(query).getSingleResult();
            assertThat(found).extracting("id", "name")
                    .containsExactly(1L, "hoho");
        }
    }

    @Test
    void rollbackLeavesNoData() {
        EntityManagerProvider<EclipseLinkEntityManagerProvider> provider = system.getComponent("eclipselink");
        try (EntityManager em = provider.createEntityManager()) {
            Person person = new Person();
            person.setId(2L);
            person.setName("rollback-me");
            em.getTransaction().begin();
            em.merge(person);
            em.getTransaction().rollback();

            assertThat(em.find(Person.class, 2L)).isNull();
        }
    }

    @Test
    void entityManagerFactoryIsClosedAfterSystemStop() {
        EclipseLinkEntityManagerProvider provider = system.getComponent("eclipselink");
        EntityManagerFactory emf = provider.getEntityManagerFactory();
        assertThat(emf.isOpen()).isTrue();

        system.stop();
        system = null; // prevent @AfterEach from calling stop() again

        assertThat(emf.isOpen()).isFalse();
    }

    @Test
    void multipleEntityManagersCanBeCreated() {
        EntityManagerProvider<EclipseLinkEntityManagerProvider> provider = system.getComponent("eclipselink");
        try (EntityManager em1 = provider.createEntityManager();
             EntityManager em2 = provider.createEntityManager()) {
            assertThat(em1).isNotSameAs(em2);
            assertThat(em1.isOpen()).isTrue();
            assertThat(em2.isOpen()).isTrue();
        }
    }
}
