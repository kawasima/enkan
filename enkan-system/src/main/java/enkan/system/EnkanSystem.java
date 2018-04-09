package enkan.system;

import enkan.component.ComponentRelationship;
import enkan.component.LifecycleManager;
import enkan.component.SystemComponent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enkan system.
 *
 * @author kawasima
 */
public class EnkanSystem {
    private Map<String, SystemComponent> components;
    private LinkedList<String> componentsOrder;

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
        EnkanSystem system = new EnkanSystem();
        for(int i = 0; i < args.length; i += 2) {
            system.setComponent(args[i].toString(), (SystemComponent) args[i + 1]);
        }
        return system;
    }

    public void setComponent(String name, SystemComponent component) {
        components.put(name, component);
        componentsOrder.add(name);
    }

    /**
     * Get all components.
     *
     * @return all components
     */
    public Collection<SystemComponent> getAllComponents() {
        return components.values();
    }

    /**
     * Get a component by its name.
     *
     * @param name component name
     * @return component
     */
    @SuppressWarnings("unchecked")
    public <T extends SystemComponent> T getComponent(String name) {
        return (T) components.get(name);
    }

    public <T extends SystemComponent> T getComponent(String name, Class<? extends T> componentType) {
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
    public <T extends SystemComponent> List<T> getComponents(Class<T> componentType) {
        return components.values()
                .stream()
                .filter(componentType::isInstance)
                .map(componentType::cast)
                .collect(Collectors.toList());
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
     * Start all components
     */
    public void start() {
        componentsOrder.stream()
                .map(key -> components.get(key))
                .forEach(LifecycleManager::start);
    }

    /**
     * Stop all components
     */
    public void stop() {
        List<String> reverse = new ArrayList<>(componentsOrder);
        Collections.reverse(reverse);
        reverse.stream()
                .map(key -> components.get(key))
                .forEach(LifecycleManager::stop);
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
