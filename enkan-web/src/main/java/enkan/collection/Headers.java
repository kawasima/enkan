package enkan.collection;

/**
 * @author kawasima
 */
public class Headers extends Parameters {
    protected Headers() {

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
}
