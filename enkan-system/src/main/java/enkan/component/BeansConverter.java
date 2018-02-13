package enkan.component;

import static enkan.component.BeansConverter.CopyOption.REPLACE_ALL;

/**
 * Converts a Java bean to another Java bean.
 *
 * @author kawasima
 */
public interface BeansConverter  {
    /**
     * Copy properties from a source object to a dest object.
     *
     * @param source an Object that is a source
     * @param destination an Object that is a destination
     */
    default void copy(Object source, Object destination) {
        copy(source, destination, REPLACE_ALL);
    }
    void copy(Object source, Object destination, CopyOption option);

    /**
     * Create a destination object and copy properties from a source object.
     *
     * @param source  an Object that is a source
     * @param destinationClass an Class that is a destination
     * @param <S> a type of destination
     * @return an Object that is instance of destination class
     */
    <S> S createFrom(Object source, Class<S> destinationClass);

    enum CopyOption {
        REPLACE_ALL,
        REPLACE_NON_NULL,
        PRESERVE_NON_NULL
    }
}
