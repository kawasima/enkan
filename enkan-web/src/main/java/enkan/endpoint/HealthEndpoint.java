package enkan.endpoint;

import enkan.Endpoint;
import enkan.component.HealthCheckable;
import enkan.component.HealthStatus;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.system.EnkanSystem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An HTTP endpoint that reports the health of all system components.
 *
 * <p>Responds with a JSON body of the form:
 * <pre>{@code
 * {
 *   "status": "UP",
 *   "components": {
 *     "datasource": "UP",
 *     "cache": "DOWN"
 *   }
 * }
 * }</pre>
 *
 * <p>The overall {@code status} is {@code "UP"} (HTTP 200) when every
 * component reports {@link HealthStatus#UP}, and {@code "DOWN"} (HTTP 503)
 * when at least one component reports {@link HealthStatus#DOWN}.
 *
 * <p>Components that do not implement {@link HealthCheckable} are not included
 * in the {@code components} map but do not affect the overall status.
 *
 * <p>Usage in an {@code ApplicationFactory}:
 * <pre>{@code
 * app.use(path("^/health$"), new HealthEndpoint(system));
 * }</pre>
 *
 * @author kawasima
 */
public class HealthEndpoint implements Endpoint<HttpRequest, HttpResponse> {
    private final EnkanSystem system;

    public HealthEndpoint(EnkanSystem system) {
        this.system = system;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        Map<String, HealthStatus> componentStatuses = new LinkedHashMap<>();

        system.getComponentMap().forEach((name, component) -> {
            if (component instanceof HealthCheckable checkable) {
                HealthStatus status;
                try {
                    status = checkable.health();
                } catch (Exception e) {
                    status = HealthStatus.DOWN;
                }
                componentStatuses.put(name, status);
            }
        });

        boolean allUp = componentStatuses.values().stream()
                .allMatch(s -> s == HealthStatus.UP);
        HealthStatus overall = allUp ? HealthStatus.UP : HealthStatus.DOWN;

        String json = buildJson(overall, componentStatuses);
        HttpResponse response = HttpResponse.of(json);
        response.setStatus(allUp ? 200 : 503);
        response.getHeaders().put("Content-Type", "application/json");
        return response;
    }

    private String buildJson(HealthStatus overall, Map<String, HealthStatus> componentStatuses) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"").append(overall.name()).append("\"");
        if (!componentStatuses.isEmpty()) {
            sb.append(",\"components\":{");
            boolean first = true;
            for (Map.Entry<String, HealthStatus> entry : componentStatuses.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":\"")
                  .append(entry.getValue().name()).append("\"");
                first = false;
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }
}
