package enkan.data;

import enkan.exception.MisconfigurationException;

import java.util.UUID;

/**
 * @author kawasima
 */
public interface Traceable extends Extendable {
    default String getId() {
        String id = (String) getExtension("id");
        if (id == null) {
            id = UUID.randomUUID().toString();
            setId(id);
        }
        return id;
    }

    default void setId(String id) {
        setExtension("id", id);
    }


    default TraceLog getTraceLog() {
        Object extension = getExtension("traceLog");
        if (extension == null) {
            extension = new TraceLog();
            setExtension("traceLog", extension);
        }

        if (extension instanceof TraceLog) {
            return (TraceLog) extension;
        } else {
            throw new MisconfigurationException("core.EXTENSION_MISMATCH", extension, "TraceLog");
        }
    }
}
