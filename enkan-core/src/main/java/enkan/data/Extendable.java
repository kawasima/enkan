package enkan.data;

/**
 * @author kawasima
 */
public interface Extendable {
    Object getExtension(String name);
    void setExtension(String name, Object extension);
}
