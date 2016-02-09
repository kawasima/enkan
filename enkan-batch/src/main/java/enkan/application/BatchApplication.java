package enkan.application;

import enkan.Application;
import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.data.BatchRequest;
import enkan.data.BatchResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author kawasima
 */
public class BatchApplication implements Application<BatchRequest,BatchResponse> {
    private LinkedList<MiddlewareChain<?, ?>> middlewareStack = new LinkedList<>();

    @Override
    public <IN, OUT> void use(Predicate<IN> predicate, String middlewareName, Middleware<IN, OUT> middleware) {
        MiddlewareChain<IN, OUT> chain = new DefaultMiddlewareChain<>(predicate, middlewareName, middleware);
        if (!middlewareStack.isEmpty()) {
            middlewareStack.getLast().setNext(chain);
        }
        middlewareStack.addLast(chain);
    }

    @Override
    public BatchResponse handle(BatchRequest req) {
        return (BatchResponse) new DefaultMiddlewareChain<>(ANY, "bootstrap", (req1, next) ->
                next.next(req1)).setNext(middlewareStack.getFirst()).next(req);
    }

    @Override
    public List<MiddlewareChain<?, ?>> getMiddlewareStack() {
        return middlewareStack;
    }
}
