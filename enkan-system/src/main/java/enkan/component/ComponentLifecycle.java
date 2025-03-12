package enkan.component;

/**
 * Defines the lifecycle management contract for system components.
 * This interface provides methods to control the initialization and cleanup
 * of components within the Enkan system.
 *
 * <p>Each component in the system must implement this lifecycle to ensure proper
 * resource management and state transitions. The lifecycle consists of two main phases:</p>
 * <ul>
 *     <li>Start: Component initialization and resource allocation</li>
 *     <li>Stop: Component cleanup and resource release</li>
 * </ul>
 *
 * @param <T> The type of system component this lifecycle manages, must extend {@link SystemComponent}
 * @author kawasima
 */
public interface ComponentLifecycle<T extends SystemComponent<T>> {
    /**
     * Starts the component and initializes its resources.
     * This method should handle all necessary setup operations such as:
     * <ul>
     *     <li>Resource allocation</li>
     *     <li>Connection establishment</li>
     *     <li>State initialization</li>
     * </ul>
     *
     * @param component The component instance to start
     */
    void start(T component);

    /**
     * Stops the component and releases its resources.
     * This method should handle all necessary cleanup operations such as:
     * <ul>
     *     <li>Resource deallocation</li>
     *     <li>Connection termination</li>
     *     <li>State cleanup</li>
     * </ul>
     *
     * @param component The component instance to stop
     */
    void stop(T component);
}
