package enkan.data;

/**
 * Extendable interface.
 *
 * @author kawasima
 */
public interface Extendable {
    Object getExtension(String name);
    void setExtension(String name, Object extension);
}
