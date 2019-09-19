package enkan.middleware.devel;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.collection.Parameters;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.TraceLog;
import enkan.data.Traceable;
import enkan.endpoint.devel.TraceDetail;
import enkan.endpoint.devel.TraceList;
import enkan.endpoint.devel.TraceRouting;
import enkan.middleware.AbstractWebMiddleware;
import enkan.middleware.session.KeyValueStore;
import enkan.middleware.session.MemoryStore;
import enkan.util.MixinUtils;
import net.unit8.moshas.MoshasEngine;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Shows the trace logs of the requests.
 *
 * @author kawasima
 */
@Middleware(name = "traceWeb")
public class TraceWebMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private LinkedList<LogKey> idList;
    private KeyValueStore store;
    private String mountPath = "/x-enkan/requests";
    private TraceRouting traceRouting;
    private long storeSize = 100;

    public static class ElapseTime {
        private Long inbound;
        private Long outbound;
        private String middlewareName;

        public ElapseTime(String middlewareName) {
            this.middlewareName = middlewareName;
        }

        public void setInboundElapse(Long elapse) {
            this.inbound = elapse;
        }

        public void setOutboundElapse(Long elapse) {
            this.outbound = elapse;
        }

        public Long getInboundElapse() {
            return inbound;
        }

        public Long getOutboundElapse() {
            return outbound;
        }

        public String getMiddlewareName() {
            return middlewareName;
        }
    }

    public TraceWebMiddleware() {
        store = new MemoryStore();
        idList = new LinkedList<>();

        MoshasEngine moshas = new MoshasEngine();
        TraceList traceList = new TraceList(moshas);
        TraceDetail traceDetail = new TraceDetail(moshas);


        traceRouting = new TraceRouting(mountPath);
        traceRouting.add("/", (req, os) -> traceList.render(os, "logs", idList));
        traceRouting.add("/[a-z0-9\\-]+", (req, os) -> {
            String id = req.getUri().substring(req.getUri().lastIndexOf("/") + 1);
            RequestLog requestLog = (RequestLog) store.read(id);

            LinkedList<ElapseTime> middlewareTraces = new LinkedList<>();

            long t = 0;
            for (TraceLog.Entry e : requestLog.getInboundLog().getEntries()) {
                if (!middlewareTraces.isEmpty()) {
                    middlewareTraces.getLast().setInboundElapse(e.getTimestamp() - t);
                }
                t = e.getTimestamp();
                middlewareTraces.add(new ElapseTime(e.getMiddleware()));
            }
            int idx = middlewareTraces.size();
            for (TraceLog.Entry e : requestLog.getOutboundLog().getEntries()) {
                middlewareTraces.get(--idx).setOutboundElapse(e.getTimestamp() - t);
                t = e.getTimestamp();
            }

            traceDetail.render(os,
                    "headers", requestLog.getHeaders(),
                    "parameters", requestLog.getParameters(),
                    "traces", middlewareTraces);
        });
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        if (request.getUri().startsWith(mountPath + "/")) {
            return traceRouting.handle(request);
        } else {
            request = MixinUtils.mixin(request, Traceable.class);
            HttpResponse response = castToHttpResponse(chain.next(request));
            Traceable requestTrace  = Traceable.class.cast(request);
            Traceable responseTrace = Traceable.class.cast(response);
            synchronized (this) {
                if (idList.size() >= storeSize) {
                    LogKey oldestLogKey = idList.removeLast();
                    store.delete(oldestLogKey.getId());
                }
                idList.addFirst(new LogKey(requestTrace.getId(), request.getRequestMethod(), request.getUri()));
                store.write(requestTrace.getId(), new RequestLog(request.getHeaders(), request.getParams(),
                        requestTrace.getTraceLog(), responseTrace.getTraceLog()));
            }
            idList.add(new LogKey(requestTrace.getId(), request.getRequestMethod(), request.getUri()));

            return response;
        }
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    public static class LogKey implements Serializable, Comparable<LogKey> {
        private String id;
        private String method;
        private String uri;
        private LocalDateTime dateTime;

        public LogKey(String id, String method, String uri) {
            this.id = id;
            this.method = method;
            this.uri = uri;
            this.dateTime = LocalDateTime.now();
        }

        public String getId() {
            return id;
        }

        public String getMethod() {
            return method;
        }

        public String getUri() {
            return uri;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object another) {
            return LogKey.class.isInstance(another) && Objects.equals(this.id, ((LogKey) another).getId());
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(LogKey another) {
            return this.dateTime.compareTo(another.getDateTime());
        }
    }

    public static class RequestLog implements Serializable {
        private Headers headers;
        private Parameters parameters;
        private TraceLog inboundLog;
        private TraceLog outboundLog;

        public RequestLog(Headers headers, Parameters parameters, TraceLog inboundLog, TraceLog outboundLog) {
            this.headers = headers;
            this.parameters = parameters;
            this.inboundLog = inboundLog;
            this.outboundLog = outboundLog;
        }

        public Headers getHeaders() {
            return headers;
        }

        public Parameters getParameters() {
            return parameters;
        }

        public TraceLog getInboundLog() {
            return inboundLog;
        }

        public TraceLog getOutboundLog() {
            return outboundLog;
        }
    }

    /**
     * Set the number of stored requests.
     *
     * @param storeSize the number of stored requests
     */
    public void setStoreSize(long storeSize) {
        this.storeSize = storeSize;
    }
}
