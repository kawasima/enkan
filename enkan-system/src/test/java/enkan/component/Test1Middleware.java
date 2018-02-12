package enkan.component;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;

/**
 * @author kawasima
 */
@Middleware(name = "test1", dependencies = {"test2"})
public class Test1Middleware<NREQ, NRES> implements enkan.Middleware<String, String, NREQ, NRES> {
    @Override
    public String handle(String s, MiddlewareChain<NREQ, NRES, ?, ?> chain) {
        return s;
    }
}
