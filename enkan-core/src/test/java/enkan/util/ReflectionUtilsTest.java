package enkan.util;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class ReflectionUtilsTest {
    @Test
    public void occursIllegalAccessException() {
        try {
            ReflectionUtils.tryReflection(() -> {
                Method m = TestPrivate.class.getDeclaredMethod("forbidden");
                assertThat(m).isNotNull();
                return m.invoke(new TestPrivate());
            });
            fail("Unreachable");
        } catch (MisconfigurationException ex) {
            assertThat(ex.getCode()).isEqualTo("core.ILLEGAL_ACCESS");
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Test
    public void occursNoSuchField() {
        try {
            ReflectionUtils.tryReflection(() -> TestPrivate.class.getDeclaredField("no-such-field"));
        } catch (MisconfigurationException ex) {
            assertThat(ex.getCode()).isEqualTo("core.NO_SUCH_FIELD");
        }
    }

    @Test
    public void occursNoSuchMethod() {
        try {
            //noinspection JavaReflectionMemberAccess
            ReflectionUtils.tryReflection(() -> TestPrivate.class.getDeclaredMethod("no-such-method"));
        } catch (MisconfigurationException ex) {
            assertThat(ex.getCode()).isEqualTo("core.NO_SUCH_METHOD");
        }
    }

    @Test
    public void occursClassNotFound() {
        try {
            ReflectionUtils.tryReflection(() -> Class.forName("no.such.clazz"));
        } catch (MisconfigurationException ex) {
            assertThat(ex.getCode()).isEqualTo("core.CLASS_NOT_FOUND");
        }
    }

    @Test
    public void occursInstantiationException() {
        try {
            ReflectionUtils.tryReflection(InstantiationableClass.class::newInstance);
        } catch (MisconfigurationException ex) {
            assertThat(ex.getCode()).isEqualTo("core.INSTANTIATION");
        }

    }

    private static class InstantiationableClass {
        public InstantiationableClass(String name) {

        }
    }

    @Test
    public void occursInvocationTargetException() {
        assertThatThrownBy(() -> ReflectionUtils.tryReflection(() -> {
            InvocationTarget t = new InvocationTarget();
            return InvocationTarget.class.getMethod("throwRuntimeException").invoke(t);
        })).isExactlyInstanceOf(NumberFormatException.class);
    }

    @Test
    public void occursInvocationTargetException_checked() {
        try {
            ReflectionUtils.tryReflection(() -> {
                InvocationTarget t = new InvocationTarget();
                return InvocationTarget.class.getMethod("throwCheckedException").invoke(t);
            });
            fail("Unreachable");
        } catch (RuntimeException ex) {
            assertThat(ex.getCause()).isOfAnyClassIn(IOException.class);
        }
    }

    private static class InvocationTarget {
        public void throwRuntimeException() {
            //noinspection ResultOfMethodCallIgnored
            Long.parseLong("ABC");
        }

        public void throwCheckedException() throws IOException {
            throw new IOException("I/O Exception");
        }
    }

}
