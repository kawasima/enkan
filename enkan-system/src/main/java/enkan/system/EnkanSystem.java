package enkan.system;

import enkan.component.LifecycleManager;
import enkan.component.SystemComponent;
import enkan.exception.UnrecoverableException;
import org.eclipse.collections.impl.factory.Lists;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author kawasima
 */
public class EnkanSystem {
    Map<String, SystemComponent> components;
    List<String> componentsOrder;


    private EnkanSystem() {
        components = new HashMap<>();
        componentsOrder = new ArrayList<>();
    }

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

    public EnkanSystem relationships(ComponentRelationship... relationships) {
        for (ComponentRelationship relationship : relationships) {
            relationship.inject(components);
            relationship.sort(componentsOrder);
        }

        return this;
    }

    public void start() {
        componentsOrder.stream()
                .map(key -> components.get(key))
                .forEach(component -> LifecycleManager.start(component));
    }

    public void stop() {
        Lists.immutable.withAll(componentsOrder).asReversed()
                .toList().stream()
                .map(key -> components.get(key))
                .forEach(LifecycleManager::stop);
    }
}
