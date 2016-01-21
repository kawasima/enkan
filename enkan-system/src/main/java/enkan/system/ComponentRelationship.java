package enkan.system;

import enkan.component.SystemComponent;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class ComponentRelationship {
    private String target;
    private List<String> dependents;

    private ComponentRelationship(String target, List<String> dependents) {
        this.target = target;
        this.dependents = dependents;
    }

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
            dependencies.put(key, components.get(key));
        }
        targetComponent.setDependencies(dependencies);
    }

    public void sort(List<String> componentsOrder) {
        int targetIndex = index(componentsOrder, target);
        for (String dep : dependents) {
            int depIndex = index(componentsOrder, dep);
            if (depIndex > targetIndex) {
                Collections.swap(componentsOrder, targetIndex, depIndex);
            }
        }
    }

    public static class ComponentRelationshipBuilder {
        private String componentName;

        ComponentRelationshipBuilder(String componentName) {
            this.componentName = componentName;
        }

        public ComponentRelationship using(String... dependents) {
            ComponentRelationship relationship = new ComponentRelationship(componentName, Arrays.asList(dependents));
            return relationship;
        }
    }
}
