package enkan.component;

/**
 * @author kawasima
 */
public abstract class BeansConverter extends SystemComponent {
    public abstract void copy(Object source, Object destination);
    public abstract <T> T createFrom(Object source, Class<T> destinationClass);
}
