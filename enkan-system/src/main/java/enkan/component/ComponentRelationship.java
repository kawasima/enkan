package enkan.component;

import enkan.exception.MisconfigurationException;
import enkan.system.inject.ComponentInjector;

import java.util.*;

/**
 * A dependency representation between components.
 *
 * @author kawasima
 */
public class ComponentRelationship {
    private final String target;
    private final List<String> dependents;

    private ComponentRelationship(String target, List<String> dependents) {
        this.target = target;
        this.dependents = dependents;
    }

    /**
     * Create a ComponentRelationshipBuilder.
     *
     * @param componentName A name of target component.
     * @return relationship builder
     */
    public static ComponentRelationshipBuilder component(String componentName) {
        return new ComponentRelationshipBuilder(componentName);
    }

    private int index(List<String> list, String v) {
        for (int i = 0; i < list.size(); i++) {
            if (v.equals(list.get(i))){
                return i;
            }
        }
        return -1;
    }

    public void inject(Map<String, SystemComponent> components) {
        SystemComponent targetComponent = components.get(target);

        Map<String, SystemComponent> dependencies = new HashMap<>();
        for (String key : dependents) {
            if (!components.containsKey(key)) {
                throw new MisconfigurationException("core.COMPONENT_NOT_FOUND", key, target);
            }
            dependencies.put(key, components.get(key));
        }
        targetComponent.setDependencies(dependencies);
        new ComponentInjector(dependencies).inject(targetComponent);
    }

    /**
     * Sorts the components in a dependent component first.
     *
     * @param componentsOrder the order between components
     */
    public void sort(LinkedList<String> componentsOrder) {
        for (String dep : dependents) {
            int targetIndex = index(componentsOrder, target);
            int depIndex = index(componentsOrder, dep);
            if (depIndex > targetIndex) {
                componentsOrder.add(targetIndex, dep);
                componentsOrder.remove(depIndex + 1);
            }
        }
    }

    public static class ComponentRelationshipBuilder {
        private final String componentName;

        ComponentRelationshipBuilder(String componentName) {
            this.componentName = componentName;
        }

        public ComponentRelationship using(String... dependents) {
            return new ComponentRelationship(componentName, Arrays.asList(dependents));
        }
    }
}
