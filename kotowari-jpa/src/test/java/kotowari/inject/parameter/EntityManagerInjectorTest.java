package kotowari.inject.parameter;

import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.jpa.EntityManageable;
import enkan.util.MixinUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EntityManagerInjectorTest {
    private EntityManagerInjector injector;

    @BeforeEach
    void setUp() {
        injector = new EntityManagerInjector();
    }

    @Test
    void isApplicableForEntityManagerType() {
        HttpRequest request = new DefaultHttpRequest();
        assertThat(injector.isApplicable(EntityManager.class, request)).isTrue();
    }

    @Test
    void isNotApplicableForOtherTypes() {
        HttpRequest request = new DefaultHttpRequest();
        assertThat(injector.isApplicable(String.class, request)).isFalse();
        assertThat(injector.isApplicable(Object.class, request)).isFalse();
    }

    @Test
    void getInjectObjectReturnsEntityManagerFromEntityManageableRequest() {
        HttpRequest request = MixinUtils.mixin(new DefaultHttpRequest(), EntityManageable.class);
        EntityManager em = mock(EntityManager.class);
        ((EntityManageable) request).setEntityManager(em);

        assertThat(injector.getInjectObject(request)).isSameAs(em);
    }

    @Test
    void getInjectObjectReturnsNullWhenRequestIsNotEntityManageable() {
        HttpRequest request = new DefaultHttpRequest();
        assertThat(injector.getInjectObject(request)).isNull();
    }

    @Test
    void getInjectObjectReturnsNullForNullRequest() {
        assertThat(injector.getInjectObject(null)).isNull();
    }
}
