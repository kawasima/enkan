package enkan.component;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;

/**
 * @author kawasima
 */
@Middleware(name = "test1", dependencies = {"test2"})
public class Test1Middleware<NREQ, NRES> implements enkan.Middleware<String, String, NREQ, NRES> {
    private String additionalMessage = "";

    @Override
    public <NNREQ, NNRES> String handle(String s, MiddlewareChain<NREQ, NRES, NNREQ, NNRES> chain) {
        return additionalMessage + s;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }
}
