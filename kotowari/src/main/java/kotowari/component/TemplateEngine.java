package kotowari.component;

import enkan.component.SystemComponent;
import enkan.data.HttpResponse;

import java.util.List;
import java.util.function.Function;

/**
 * @author kawasima
 */
public abstract class TemplateEngine extends SystemComponent {
    public abstract HttpResponse render(String name, Object... keyOrVals);

    public abstract Object createFunction(Function<List, Object> func);
}
