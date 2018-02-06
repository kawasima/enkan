package enkan.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kawasima
 */
public class TraceLog implements Serializable {
    private final List<Entry> entries;

    public TraceLog() {
        entries = new ArrayList<>();
    }

    public void write(String middleware) {
        entries.add(new Entry(System.currentTimeMillis(), middleware));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry implements Serializable {
        private long timestamp;
        private String middleware;

        public Entry(long timestamp, String middleware) {
            this.timestamp = timestamp;
            this.middleware = middleware;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getMiddleware() {
            return middleware;
        }

        public void setMiddleware(String middleware) {
            this.middleware = middleware;
        }
    }
}
