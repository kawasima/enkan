package enkan.data;

import enkan.exception.MisconfigurationException;

/**
 * @author kawasima
 */
public interface Traceable extends Extendable {
    default String getId() {
        return getExtension("id").toString();
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
            throw MisconfigurationException.create("EXTENSION_MISMATCH.problem", extension, "TraceLog");
        }
    }
}
