package enkan.component;

/**
 * Implemented by components that can report their own health status.
 *
 * <p>When an {@code EnkanSystem} is queried by a health-check endpoint,
 * components that implement this interface are asked for their current
 * {@link HealthStatus}.  Components that do <em>not</em> implement this
 * interface are assumed to be {@link HealthStatus#UP} as long as the system
 * has been started without throwing an exception.
 *
 * @author kawasima
 */
public interface HealthCheckable {

    /**
     * Returns the current health status of this component.
     *
     * <p>Implementations should be lightweight and fast: this method is called
     * on every health-check request.  Avoid blocking I/O where possible; if a
     * quick connectivity probe is necessary (e.g. a JDBC {@code isValid} check)
     * keep the timeout short.
     *
     * @return {@link HealthStatus#UP} if the component is operating normally,
     *         {@link HealthStatus#DOWN} otherwise
     */
    HealthStatus health();
}
