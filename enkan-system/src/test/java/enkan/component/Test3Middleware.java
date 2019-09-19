package enkan.component;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;

@Middleware(name = "test3")
public class Test3Middleware<NREQ, NRES> implements enkan.Middleware<String, String, NREQ, NRES> {
    private String additionalMessage = "";

    @Override
    public String handle(String s, MiddlewareChain<NREQ, NRES, ?, ?> chain) {
        return additionalMessage + s;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }
}
