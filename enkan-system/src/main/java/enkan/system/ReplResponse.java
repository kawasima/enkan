package enkan.system;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static enkan.system.ReplResponse.ResponseStatus.*;

/**
 * @author kawasima
 */
public class ReplResponse implements Serializable {
    private String id;
    private Set<ResponseStatus> status;
    private String value;
    private String out;
    private String err;

    public ReplResponse() {
        status = new HashSet<>();
    }

    private ReplResponse(UUID id) {
        this();
        this.id = id.toString();
    }

    public static ReplResponse forRestore(UUID id) {
        return new ReplResponse(id);
    }

    public static ReplResponse withOut(String message) {
        ReplResponse response = new ReplResponse();
        response.setOut(message);
        return response;
    }

    public static ReplResponse withErr(String message) {
        ReplResponse response = new ReplResponse();
        response.setErr(message);
        response.status.add(ERROR);
        return response;
    }


    public enum ResponseStatus {
        SHUTDOWN, UNKNOWN_COMMAND, ERROR, DONE
    }

    public String getId() {
        return id;
    }

    public Set<ResponseStatus> getStatus() {
        return status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public ReplResponse done() {
        this.status.add(DONE);
        return this;
    }
}
