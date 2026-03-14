package kotowari.graalvm;

/**
 * Holds the singleton {@link KotowariDispatcherInvoker} instance generated at build time.
 * {@link KotowariFeature} populates this at native image build time;
 * {@link NativeControllerInvokerMiddleware} reads it at runtime.
 */
public final class NativeDispatcherRegistry {
    private static volatile KotowariDispatcherInvoker invoker;

    private NativeDispatcherRegistry() {}

    public static void register(KotowariDispatcherInvoker inv) {
        invoker = inv;
    }

    public static KotowariDispatcherInvoker get() {
        return invoker;
    }
}
