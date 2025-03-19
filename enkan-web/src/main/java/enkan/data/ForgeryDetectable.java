package enkan.data;

/**
 * @author kawasima
 */
public interface ForgeryDetectable extends Extendable {
    default String getAntiForgeryToken() {
        return getExtension("antiForgeryToken");
    }

    default void setAntiForgeryToken(String token) {
        setExtension("antiForgeryToken", token);
    }
}
