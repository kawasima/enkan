package enkan.system.repl;

import enkan.system.Transport;

/**
 * Context passed to {@link TransportProvider} implementations,
 * giving them access to REPL command dispatch and I/O broadcast.
 *
 * @author kawasima
 */
public interface TransportContext {
    /**
     * Dispatch a command string (e.g. "/help", "1+1") on the given transport.
     *
     * @param message the raw command string from the client
     * @param transport the transport to send responses on
     */
    void dispatch(String message, Transport transport);

    /**
     * Register a transport to receive broadcast output (stdout/stderr from JShell).
     *
     * @param key unique key identifying this transport session
     * @param transport the transport to broadcast to
     */
    void registerBroadcast(Object key, Transport transport);

    /**
     * Unregister a broadcast transport.
     *
     * @param key the key used when registering
     */
    void unregisterBroadcast(Object key);
}
