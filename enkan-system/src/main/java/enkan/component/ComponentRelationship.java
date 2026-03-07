package enkan.component;

import enkan.exception.MisconfigurationException;
import enkan.system.inject.ComponentInjector;

import java.util.*;

/**
 * A dependency representation between components.
 *
 * @author kawasima
 */
public record ComponentRelationship(String target, List<String> dependents) {
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

    public void inject(Map<String, SystemComponent<?>> components) {
        SystemComponent<?> targetComponent = Optional.ofNullable(components.get(target))
                .orElseThrow(() -> new MisconfigurationException("core.COMPONENT_NOT_FOUND", target, target));

        Map<String, SystemComponent<?>> dependencies = targetComponent.getAllDependencies();
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
     * Sorts the components so that each dependent appears before the target.
     * Iterates until the order is stable to handle transitive dependencies.
     *
     * @param componentsOrder the order between components
     */
    public void sort(LinkedList<String> componentsOrder) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String dep : dependents) {
                int targetIndex = index(componentsOrder, target);
                int depIndex = index(componentsOrder, dep);
                if (targetIndex == -1) {
                    throw new MisconfigurationException("core.COMPONENT_NOT_FOUND", target, target);
                }
                if (depIndex == -1) {
                    throw new MisconfigurationException("core.COMPONENT_NOT_FOUND", dep, target);
                }
                if (depIndex > targetIndex) {
                    componentsOrder.add(targetIndex, dep);
                    componentsOrder.remove(depIndex + 1);
                    changed = true;
                }
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
