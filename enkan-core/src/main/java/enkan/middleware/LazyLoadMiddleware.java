package enkan.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * Load on demand from the name of the middleware class.
 *
 * @param <REQ>  A type of the request
 * @param <RES>  A type of the response
 * @param <NREQ> A type of the next request
 * @param <NRES>A type of the next response
 *
 * @author kawasima
 */
public class LazyLoadMiddleware<REQ, RES, NREQ, NRES> implements Middleware<REQ, RES, NREQ, NRES> {
    private Middleware<REQ, RES, NREQ, NRES> instance;
    private Lock initializingLock = new ReentrantLock();
    private String middlewareClassName;

    public LazyLoadMiddleware(String middlewareClassName) {
        this.middlewareClassName = middlewareClassName;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public RES handle(REQ request, MiddlewareChain<NREQ, NRES, ?, ?> chain) {
        if (instance == null) {
            try {
                initializingLock.lock();
                instance = tryReflection(() -> {
                    Class<Middleware<REQ, RES, NREQ, NRES>> middlewareClass = (Class<Middleware<REQ, RES, NREQ, NRES>>) Class.forName(middlewareClassName);
                    return middlewareClass.getConstructor().newInstance();
                });
            } finally {
                initializingLock.unlock();
            }
        }
        return instance.handle(request, chain);
    }
}
