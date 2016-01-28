package enkan.middleware.session;

import enkan.data.Session;

/**
 * @author kawasima
 */
public interface SessionStore {
    /**
     * Read a session.
     *
     * @param key session key
     * @return session object
     */
    Session read(String key);

    /**
     * Write a session.
     *
     * @param key session key
     * @param session session object
     * @return new session key
     */
    String write(String key, Session session);

    /**
     * Delete a session.
     *
     * @param key session key
     * @return session key
     */
    String delete(String key);
}
