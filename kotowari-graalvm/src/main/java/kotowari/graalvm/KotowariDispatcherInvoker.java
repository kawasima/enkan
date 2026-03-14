package kotowari.graalvm;

/**
 * Functional interface implemented by the build-time-generated {@code KotowariDispatcher}.
 * Using this interface eliminates {@code Method.invoke} on the request hot path — the JVM
 * emits a direct {@code invokeinterface} call site.
 */
public interface KotowariDispatcherInvoker {
    Object dispatch(String key, Object controller, Object[] args);
}
