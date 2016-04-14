package enkan.predicate;

import enkan.data.Extendable;
import enkan.data.PrincipalAvailable;
import enkan.security.UserPrincipal;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author kawasima
 */
public class PermissionPredicateTest {
    @Data
    static class PrincipalRequest implements PrincipalAvailable, Extendable {
        private Map<String, Object> extensions = new HashMap<>();

        @Override
        public Object getExtension(String name) {
            return extensions.get(name);
        }

        @Override
        public void setExtension(String name, Object extension) {
            extensions.put(name, extension);
        }
    }

    @Data
    @RequiredArgsConstructor
    static class TestUser implements UserPrincipal {
        @NonNull
        private String name;
        @NonNull
        private Set<String> permissions;

        @Override
        public boolean hasPermission(String permission) {
            return permissions.contains(permission);
        }
    }

    @Test
    public void test() {
        PermissionPredicate<PrincipalRequest> pred = new PermissionPredicate<>("deleteRepository");
        PrincipalRequest req = new PrincipalRequest();
        TestUser user = new TestUser("kawasima", new HashSet<>(Arrays.asList("deleteRepository", "createRepository")));
        req.setPrincipal(user);
        assertTrue(pred.test(req));

        TestUser user2 = new TestUser("kawasima", new HashSet<>(Arrays.asList("readRepository")));
        req.setPrincipal(user2);
        assertFalse(pred.test(req));
    }

}
