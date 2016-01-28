package enkan.data;

/**
 * @author kawasima
 */
public interface SessionAvailable extends Extendable {
    Session getSession();
    void setSession(Session session);
}
