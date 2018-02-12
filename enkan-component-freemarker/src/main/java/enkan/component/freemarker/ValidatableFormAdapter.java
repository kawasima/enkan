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
    private TemplateMethodModelEx hasErrors;
    private TemplateMethodModelEx getErrors;

    public ValidatableFormAdapter(Validatable form, DefaultObjectWrapper ow) {
        super(form, ow);
        this.hasErrors = arguments -> {
            switch (arguments.size()) {
                case 0:
                    return (Boolean) form.hasErrors();
                case 1:
                    return (Boolean) form.hasErrors(arguments.get(0).toString());
                default:
                    return null;
            }
        };

        this.getErrors = arguments -> {
            switch (arguments.size()) {
                case 0:
                    return form.getErrors();
                case 1:
                    return form.getErrors(arguments.get(0).toString());
                default:
                    return null;
            }
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
