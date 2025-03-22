package enkan.component.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import kotowari.data.Validatable;

/**
 * Form object model.
 *
 * @author kawasima
 */
public class ValidatableFormAdapter extends BeanModel {
    private final TemplateMethodModelEx hasErrors;
    private final TemplateMethodModelEx getErrors;

    public ValidatableFormAdapter(Validatable form, DefaultObjectWrapper ow) {
        super(form, ow);
        this.hasErrors = arguments -> switch (arguments.size()) {
            case 0 -> (Boolean) form.hasErrors();
            case 1 -> (Boolean) form.hasErrors(arguments.getFirst().toString());
            default -> null;
        };

        this.getErrors = arguments -> switch (arguments.size()) {
            case 0 -> form.getErrors();
            case 1 -> form.getErrors(arguments.getFirst().toString());
            default -> null;
        };
    }

    public TemplateModel get(String key)
            throws
            TemplateModelException {
        TemplateModel model = super.get(key);
        if (model == null) {
            switch(key) {
                case "hasErrors":
                    return hasErrors;
                case "getErrors":
                    return getErrors;
            }
        }
        return model;
    }
}
