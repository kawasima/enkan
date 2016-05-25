package enkan.component;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;

/**
 * @author kawasima
 */
@Middleware(name = "test2")
public class Test2Middleware implements enkan.Middleware<String, String> {
    @Override
    public String handle(String s, MiddlewareChain chain) {
        return s;
    }
}
