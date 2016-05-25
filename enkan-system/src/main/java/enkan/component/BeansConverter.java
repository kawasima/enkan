package enkan.component;

/**
 * Converts a Java bean to another Java bean.
 *
 * @author kawasima
 */
public abstract class BeansConverter extends SystemComponent {
    /**
     * Copy properties from a source object to a dest object.
     *
     * @param source an Object that is a source
     * @param destination an Object that is a destination
     */
    public abstract void copy(Object source, Object destination);

    /**
     * Create a destination object and copy properties from a source object.
     *
     * @param source  an Object that is a source
     * @param destinationClass an Class that is a destination
     * @param <T> a type of destination
     * @return an Object that is instance of destination class
     */
    public abstract <T> T createFrom(Object source, Class<T> destinationClass);
}
