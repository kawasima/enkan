package enkan.system;

import enkan.component.ComponentRelationship;
import enkan.component.LifecycleManager;
import enkan.component.SystemComponent;
import enkan.exception.MisconfigurationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The top-level container that owns and orchestrates all system components.
 *
 * <p>An {@code EnkanSystem} is constructed via the static factory
 * {@link #of(Object...)} which accepts interleaved name/component pairs:
 *
 * <pre>{@code
 * EnkanSystem system = EnkanSystem.of(
 *     "datasource", new HikariCPComponent(...),
 *     "jpa",        new JpaProvider(...),
 *     "app",        new ApplicationComponent(...)
 * ).relationships(
 *     ComponentRelationship.component("jpa").using("datasource"),
 *     ComponentRelationship.component("app").using("jpa")
 * );
 * system.start();
 * }</pre>
 *
 * <p>Components are started in declaration order (respecting the dependency
 * order established by {@link #relationships}) and stopped in reverse order,
 * ensuring a clean shutdown even when components depend on one another.
 *
 * @author kawasima
 */
public class EnkanSystem {
    private final Map<String, SystemComponent<?>> components;
    private final LinkedList<String> componentsOrder;
    private boolean started = false;

    private EnkanSystem() {
        components = new HashMap<>();
        componentsOrder = new LinkedList<>();
    }

    /**
     * Create an enkan system.
     *
     * @param args definitions of components.
     * @return enkan system
     */
    public static EnkanSystem of(Object... args) {
        if (args.length % 2 != 0) {
            throw new MisconfigurationException("core.INVALID_SYSTEM_ARGS", args.length);
        }
        EnkanSystem system = new EnkanSystem();
        for(int i = 0; i < args.length; i += 2) {
            if (!(args[i + 1] instanceof SystemComponent)) {
                throw new MisconfigurationException("core.INVALID_COMPONENT", args[i], args[i + 1].getClass().getName());
            }
            system.setComponent(args[i].toString(), (SystemComponent<?>) args[i + 1]);
        }
        return system;
    }

    /**
     * Registers a component under the given name.
     *
     * <p>Components are started and stopped in the order they are registered
     * (subject to any ordering imposed by {@link #relationships}).
     *
     * @param <T>       the self-type of the component
     * @param name      the unique name used to look up this component
     * @param component the component instance to register
     */
    public <T extends SystemComponent<T>> void setComponent(String name, SystemComponent<T> component) {
        components.put(name, component);
        componentsOrder.add(name);
    }

    /**
     * Get all components.
     *
     * @return all components
     */
    public Collection<SystemComponent<?>> getAllComponents() {
        return components.values();
    }

    /**
     * Returns an unmodifiable view of the name-to-component map,
     * preserving the registration order.
     *
     * @return map of component name to component instance
     */
    public Map<String, SystemComponent<?>> getComponentMap() {
        Map<String, SystemComponent<?>> ordered = new LinkedHashMap<>();
        componentsOrder.forEach(name -> ordered.put(name, components.get(name)));
        return Collections.unmodifiableMap(ordered);
    }

    /**
     * Get a component by its name.
     *
     * @param name component name
     * @return component
     */
    @SuppressWarnings("unchecked")
    public <T extends SystemComponent<T>> T getComponent(String name) {
        return (T) components.get(name);
    }

    /**
     * Returns the component registered under {@code name}, cast to
     * {@code componentType}, or {@code null} if no matching component exists.
     *
     * @param <T>           the expected component type
     * @param name          the registered component name
     * @param componentType the class of the expected component type
     * @return the component, or {@code null}
     */
    public <T extends SystemComponent<T>> T getComponent(String name, Class<? extends T> componentType) {
        return components.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(name))
                .map(Map.Entry::getValue)
                .filter(componentType::isInstance)
                .map(componentType::cast)
                .findAny()
                .orElse(null);
    }
    /**
     * Get a components by the type.
     *
     * @param componentType component type
     * @return A list of components
     */
    public <T extends SystemComponent<T>> List<T> getComponents(Class<T> componentType) {
        return components.values()
                .stream()
                .filter(componentType::isInstance)
                .map(componentType::cast)
                .toList();
    }

    /**
     * Setting relationships between component.
     *
     * @param relationships component relationship
     * @return this system
     */
    public EnkanSystem relationships(ComponentRelationship... relationships) {
        for (ComponentRelationship relationship : relationships) {
            relationship.inject(components);
            relationship.sort(componentsOrder);
        }

        return this;
    }

    /**
     * Returns whether the system has been started.
     *
     * @return {@code true} if {@link #start()} has been called and {@link #stop()} has not
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Start all components
     */
    public void start() {
        componentsOrder.stream()
                .map(components::get)
                .forEach(EnkanSystem::startComponent);
        started = true;
    }

    /**
     * Stop all components
     */
    public void stop() {
        List<String> reverse = new ArrayList<>(componentsOrder);
        Collections.reverse(reverse);
        reverse.stream()
                .map(components::get)
                .forEach(EnkanSystem::stopComponent);
        started = false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends SystemComponent<T>> void startComponent(SystemComponent<?> component) {
        LifecycleManager.start((T) component);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SystemComponent<T>> void stopComponent(SystemComponent<?> component) {
        LifecycleManager.stop((T) component);
    }

    @Override
    public String toString() {
        String out = componentsOrder.stream()
                .map(name -> "  \"" + name + "\": " +
                        Arrays.stream(components.get(name).toString().split("\n"))
                                .map(line -> "  " + line)
                                .collect(Collectors.joining("\n")))
                .collect(Collectors.joining(",\n"));

        if (out.isEmpty()) {
            return "EnkanSystem {}";
        } else {
            return "EnkanSystem {\n" + out + "\n}";
        }
    }
}
