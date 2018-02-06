package enkan.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static enkan.util.ReflectionUtils.tryReflection;

public class LazyLoadMiddleware<REQ, RES> implements Middleware<REQ, RES> {
    private Middleware<REQ, RES> instance;
    private Lock initializingLock = new ReentrantLock();
    private String middlewareClassName;

    public LazyLoadMiddleware(String middlewareClassName) {
        this.middlewareClassName = middlewareClassName;
    }

    @Override
    public RES handle(REQ request, MiddlewareChain chain) {
        if (instance == null) {
            try {
                initializingLock.lock();
                instance = tryReflection(() -> {
                    Class<Middleware<REQ, RES>> middlewareClass = (Class<Middleware<REQ, RES>>) Class.forName(middlewareClassName);
                    return middlewareClass.getConstructor().newInstance();
                });
            } finally {
                initializingLock.unlock();
            }
        }
        return instance.handle(request, chain);
    }
}
