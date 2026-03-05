package kotowari.example;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * A SpanExporter that writes completed spans to SLF4J at INFO level.
 */
public class Slf4jSpanExporter implements SpanExporter {
    private static final Logger LOG = LoggerFactory.getLogger("otel.spans");

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        for (SpanData span : spans) {
            LOG.info("trace_id={} span_id={} parent_span_id={} name=\"{}\" kind={} status={} duration_ms={} attributes={}",
                    span.getTraceId(),
                    span.getSpanId(),
                    span.getParentSpanId(),
                    span.getName(),
                    span.getKind(),
                    span.getStatus().getStatusCode(),
                    (span.getEndEpochNanos() - span.getStartEpochNanos()) / 1_000_000,
                    span.getAttributes());
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }
}
