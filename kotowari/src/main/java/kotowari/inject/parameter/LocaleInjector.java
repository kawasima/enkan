package kotowari.inject.parameter;

import enkan.data.ContentNegotiable;
import enkan.data.HttpRequest;
import kotowari.inject.ParameterInjector;

import java.util.Locale;

public class LocaleInjector implements ParameterInjector<Locale> {
    @Override
    public String getName() {
        return "Locale";
    }

    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        return Locale.class.equals(type);
    }

    @Override
    public Locale getInjectObject(HttpRequest request) {
        if (request instanceof ContentNegotiable) {
            return ContentNegotiable.class.cast(request).getLocale();
        }
        return null;
    }
}
