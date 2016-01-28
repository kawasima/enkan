package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author kawasima
 */
public class PreventDDoSMiddleWare extends AbstractWebMiddleware {
    private LRUCache cache;


    private int threshold = 10000;
    private int banThreshold = 10000;
    private int period = 10;
    private int banPeriod = 300;
    private int tableSize = 100;


    public PreventDDoSMiddleWare() {
        cache = new LRUCache(tableSize);
    }


    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        String remoteAddr = request.getRemoteAddr();
        if (request.getHeaders().containsKey("X-Forwarded-For")) {
            remoteAddr = request.getHeaders().get("X-Forwarded-For");
        }

        AccessClient ac = cache.get(remoteAddr);
        long now = System.currentTimeMillis();
        if (ac != null) {
            if (ac.getSuspected() > 0 && ac.getSuspected() + banPeriod > now) {
                if (ac.getCount() > banThreshold) {
                    ac.setHardSuspected(now);
                }
            } else {
                if (ac.getSuspected() > 0) {
                    ac.setSuspected(0);
                    ac.setHardSuspected(0);
                    ac.setCount(0);
                }
                if (ac.getCount() > threshold) {
                   ac.setSuspected(now);
                }
            }
        }
        return null;
    }

    private static class AccessClient {
        private String remoteAddr;
        private int count;
        private long interval;
        private long last;
        private long suspected;
        private long hardSuspected;

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public void setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public long getLast() {
            return last;
        }

        public void setLast(long last) {
            this.last = last;
        }

        public long getSuspected() {
            return suspected;
        }

        public void setSuspected(long suspected) {
            this.suspected = suspected;
        }

        public long getHardSuspected() {
            return hardSuspected;
        }

        public void setHardSuspected(long hardSuspected) {
            this.hardSuspected = hardSuspected;
        }
    }

    private static class LRUCache {
        private final int capacity;
        private final ConcurrentLinkedQueue<String> keyQueue;
        private final ConcurrentHashMap<String, AccessClient> internalMap;

        public LRUCache(final int capacity) {
            this.capacity = capacity;
            keyQueue = new ConcurrentLinkedQueue<>();
            internalMap = new ConcurrentHashMap<>();
        }

        public AccessClient get(String ip) {
            return internalMap.get(ip);
        }

        public void put(String ip, AccessClient ac) {
            if (internalMap.containsKey(ip)) {
                keyQueue.remove(ip);
            }

            while (keyQueue.size() > capacity) {
                String oldestKey = keyQueue.poll();
                if (oldestKey != null) {
                    internalMap.remove(oldestKey);
                }
            }

            keyQueue.add(ip);
            internalMap.put(ip, ac);
        }
    }
}
