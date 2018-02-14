package kotowari.routing;

import enkan.collection.Headers;
import enkan.collection.OptionMap;
import enkan.data.HttpResponse;
import enkan.util.HttpResponseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static enkan.util.BeanBuilder.builder;

/**
 * @author kawasima
 */
public class UrlRewriter {
    public static RoutingGenerationContext urlFor(OptionMap options) {
        return new RoutingGenerationContext(options);
    }

    public static RoutingGenerationContext urlFor(Class<?> controller, String optionString) {
        OptionMap options = parseOptionString(controller, optionString);
        return urlFor(options);
    }

    public static HttpResponse redirect(Class<?> controller, String options, HttpResponseUtils.RedirectStatusCode code) {
        return builder(HttpResponse.of(""))
                .set(HttpResponse::setStatus, code.getStatusCode())
                .set(HttpResponse::setHeaders, Headers.of("Location", urlFor(controller, options)))
                .build();
    }

    @SuppressWarnings("unchecked")
    public static OptionMap parseOptionString(Class<?> controller, String optionString) {
        String[] urlTokens = optionString.split("\\?", 2);

        OptionMap options = OptionMap.of("controller", controller, "action", urlTokens[0]);

        if (urlTokens.length == 2 && !urlTokens[1].isEmpty()) {
            String[] paramToken = urlTokens[1].split("&");
            Arrays.stream(paramToken)
                    .map(kv -> kv.split("=", 2))
                    .forEach(pair -> {
                        if (pair.length == 1) {
                            options.put(pair[0], null);
                        } else if (pair.length == 2) {
                            Object value = options.get(pair[0]);
                            if (value == null) {
                                options.put(pair[0], pair[1]);
                            } else if (value instanceof List) {
                                ((List<String>) value).add(pair[1]);
                            } else {
                                List<String> values = new ArrayList<>();
                                values.add(value.toString());
                                values.add(pair[1]);
                                options.put(pair[0], values);
                            }
                        }
                    });
        }
        return options;
    }
}
