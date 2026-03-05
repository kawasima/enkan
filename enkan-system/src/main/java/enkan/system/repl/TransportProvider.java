package enkan.system.repl;

/**
 * Provides an additional transport layer for the REPL server.
 * Implementations start their own server (e.g. WebSocket) and dispatch
 * incoming messages to the REPL's command handler.
 *
 * @author kawasima
 */
public interface TransportProvider {
    /**
     * Start this transport.
     *
     * @param context provides access to REPL internals (command dispatch, I/O broadcast)
     */
    void start(TransportContext context);

    /**
     * Stop this transport and release resources.
     */
    void stop();
}
