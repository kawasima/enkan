package enkan.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kawasima
 */
public class TraceLog implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public record Entry(long timestamp, String middleware) implements Serializable {
    }
}
