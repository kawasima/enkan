package kotowari.middleware;

import enkan.exception.MisconfigurationException;
import enkan.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

public class RenderTemplateMiddlewareTest {
    private static Function<List, Object> HAS_ANY_PERMISSIONS = arguments -> {
        if (arguments.size() >= 2) {
            Object principal = arguments.get(0);
            if (principal instanceof UserPrincipal) {
                return arguments.subList(1, arguments.size())
                        .stream()
                        .anyMatch(p -> ((UserPrincipal) principal).hasPermission(Objects.toString(p)));
            } else {
                throw new MisconfigurationException("kotowari.HAS_PERMISSION_FIRST_ARG", "hasAnyPermission");
            }
        } else {
            throw new MisconfigurationException("kotowari.HAS_ANY_PERMISSION_WRONG_ARGS");
        }
    };

    @Test
    public void hasAnyPermissions() {
        Object ret = HAS_ANY_PERMISSIONS.apply(Arrays.asList(
                new UserPrincipal() {
                    @Override
                    public boolean hasPermission(String permission) {
                        return Objects.equals("PERM1", permission);
                    }

                    @Override
                    public String getName() {
                        return "test";
                    }
                }
        , "PERM1"));
        assertThat(ret instanceof Boolean).isTrue();
        assertThat((Boolean) ret).isTrue();
    }
}
