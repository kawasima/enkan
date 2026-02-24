package kotowari.example.controller.api;

import enkan.collection.Parameters;
import kotowari.example.model.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TodoApiController {
    private static final AtomicLong idGenerator = new AtomicLong(1);
    private static final Map<Long, Todo> store = new ConcurrentHashMap<>();

    public List<Todo> list() {
        return new ArrayList<>(store.values());
    }

    public Todo show(Parameters params) {
        Long id = params.getLong("id");
        return store.get(id);
    }

    public Todo create(Todo todo) {
        Long id = idGenerator.getAndIncrement();
        todo.setId(id);
        store.put(id, todo);
        return todo;
    }

    public Todo update(Parameters params, Todo todo) {
        Long id = params.getLong("id");
        todo.setId(id);
        store.put(id, todo);
        return todo;
    }

    public Todo delete(Parameters params) {
        Long id = params.getLong("id");
        return store.remove(id);
    }
}
