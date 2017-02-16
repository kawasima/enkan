package enkan.util;

import enkan.exception.MisconfigurationException;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ReflectionUtilsTest {
    @Test
    public void occursIllegalAccessException() {
        try {
            ReflectionUtils.tryReflection(() -> {
                Method m = TestPrivate.class.getDeclaredMethod("forbidden");
                assertNotNull(m);
                return m.invoke(new TestPrivate());
            });
            fail();
        } catch (MisconfigurationException ex) {
            assertEquals("core.ILLEGAL_ACCESS", ex.getCode());
        }
    }

    @Test
    public void occursNoSuchField() {
        try {
            ReflectionUtils.tryReflection(() -> TestPrivate.class.getDeclaredField("no-such-field"));
        } catch (MisconfigurationException ex) {
            assertEquals("core.NO_SUCH_FIELD", ex.getCode());
        }
    }

    @Test
    public void occursNoSuchMethod() {
        try {
            ReflectionUtils.tryReflection(() -> TestPrivate.class.getDeclaredMethod("no-such-method"));
        } catch (MisconfigurationException ex) {
            assertEquals("core.NO_SUCH_METHOD", ex.getCode());
        }
    }

    @Test
    public void occursClassNotFound() {
        try {
            ReflectionUtils.tryReflection(() -> Class.forName("no.such.clazz"));
        } catch (MisconfigurationException ex) {
            assertEquals("core.CLASS_NOT_FOUND", ex.getCode());
        }
    }

    @Test
    public void occursInstantiationException() {
        try {
            ReflectionUtils.tryReflection(InstantiationableClass.class::newInstance);
        } catch (MisconfigurationException ex) {
            assertEquals("core.INSTANTIATION", ex.getCode());
        }

    }

    private static class InstantiationableClass {
        public InstantiationableClass(String name) {

        }
    }

    @Test(expected = NumberFormatException.class)
    public void occursInvocationTargetException() {
        ReflectionUtils.tryReflection(() -> {
            InvocationTarget t = new InvocationTarget();
            return InvocationTarget.class.getMethod("throwRuntimeException").invoke(t);
        });
    }

    @Test
    public void occursInvocationTargetException_checked() {
        try {
            ReflectionUtils.tryReflection(() -> {
                InvocationTarget t = new InvocationTarget();
                return InvocationTarget.class.getMethod("throwCheckedException").invoke(t);
            });
            fail();
        } catch (RuntimeException ex) {
            assertEquals(IOException.class, ex.getCause().getClass());
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
