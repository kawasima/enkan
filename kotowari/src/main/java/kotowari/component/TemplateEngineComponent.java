package kotowari.component;

import enkan.component.SystemComponent;
import enkan.data.HttpResponse;

/**
 * @author kawasima
 */
public abstract class TemplateEngineComponent extends SystemComponent {
    public abstract HttpResponse render(String name, Object... keyOrVals);
}
