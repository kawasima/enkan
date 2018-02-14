package enkan.data;

/**
 * Extendable interface.
 *
 * @author kawasima
 */
public interface Extendable {
    <T> T getExtension(String name);
    <T> void setExtension(String name, T extension);
}
