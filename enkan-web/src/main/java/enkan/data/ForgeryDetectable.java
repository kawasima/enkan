package enkan.data;

/**
 * @author kawasima
 */
public interface ForgeryDetectable extends Extendable {
    default String getAntiForgeryToken() {
        return (String) getExtension("antiForgeryToken");
    }

    default void setAntiForgeryToken(String token) {
        setExtension("antiForgeryToken", token);
    }
}
