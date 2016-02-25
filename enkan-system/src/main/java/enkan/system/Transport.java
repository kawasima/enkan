package enkan.system;

import enkan.system.ReplResponse.ResponseStatus;

import java.util.Arrays;

import static enkan.system.ReplResponse.ResponseStatus.*;
/**
 * @author kawasima
 */
public interface Transport {
    void send(ReplResponse response);
    String recv(long timeout);

    default void sendOut(String message) {
        sendOut(message, DONE);
    }

    default void sendOut(String message, ResponseStatus... statuses) {
        ReplResponse res = ReplResponse.withOut(message);
        res.getStatus().addAll(Arrays.asList(statuses));
        send(res);
    }

    default void sendErr(String errMessage, ResponseStatus... statuses) {
        ReplResponse res = ReplResponse.withErr(errMessage);
        res.getStatus().addAll(Arrays.asList(statuses));
        res.getStatus().add(DONE);
        res.getStatus().add(ERROR);
        send(res);
    }

    default String recv() {
        return recv(-1);
    }

}
