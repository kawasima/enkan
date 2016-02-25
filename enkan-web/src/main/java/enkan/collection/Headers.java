package enkan.collection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class Headers extends Parameters {
    private static final Map<String, String> KEYWORDS = Arrays.asList("CSP", "ATT", "WAP", "IP", "HTTP", "CPU", "DNT", "SSL", "UA", "TE", "WWW", "XSS", "MD5")
            .stream()
            .collect(Collectors.toMap(k -> k, k -> k));


    protected Headers() {
        setCaseSensitive(false);
    }

    public static Headers empty() {
        return new Headers();
    }

    public static Headers of(String k1, Object v1) {
        Headers headers = empty();
        headers.put(k1, v1);
        return headers;
    }

    public static Headers of(String k1, Object v1, String k2, Object v2) {
        Headers headers = Headers.of(k1, v1);
        headers.put(k2, v2);
        return headers;
    }

    public static Headers of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        Headers headers = Headers.of(k1, v1, k2, v2);
        headers.put(k3, v3);
        return headers;
    }

    public static Headers of(String k1, Object v1, String k2, Object v2, String k3, Object v3,  String k4, Object v4) {
        Headers headers = Headers.of(k1, v1, k2, v2, k3, v3);
        headers.put(k4, v4);
        return headers;
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = super.keySet();
        Set<String> headerKeys = new HashSet<>(keys.size() + 10);
        for (String key : keys) {
            headerKeys.add(Arrays.stream(key.split("-"))
                        .map(t -> {
                            if (t.length() < 2) {
                                return t.toUpperCase(Locale.US);
                            } else {
                                return Optional.ofNullable(KEYWORDS.get(t.toUpperCase(Locale.US)))
                                        .orElseGet(() -> Character.toUpperCase(t.charAt(0)) + t.substring(1));
                            }
                        })
                        .collect(Collectors.joining("-")));
        }
        return headerKeys;
    }

}
