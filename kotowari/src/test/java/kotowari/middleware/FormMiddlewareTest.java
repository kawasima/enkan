package kotowari.middleware;

import enkan.collection.Multimap;
import enkan.collection.NestedParams;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.middleware.NestedParamsMiddleware;
import enkan.middleware.ParamsMiddleware;
import kotowari.test.form.NestedForm;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class FormMiddlewareTest extends FormMiddleware {
    private static final Pattern RE_NESTED_NAME = Pattern.compile("^(?s)(.*?)((?:\\[.*?\\])*)$");
    private static final Pattern RE_NESTED_TOKEN = Pattern.compile("\\[(.*?)\\]");
    protected Function<String, String[]> parseNestedKeys = (paramName) -> {
        if (paramName == null) return new String[]{};

        Matcher m = RE_NESTED_NAME.matcher(paramName);
        List<String> keys = new ArrayList<>();

        if (m.find()) {
            keys.add(m.group(1));
            String ks = m.group(2);
            if (ks != null && !ks.isEmpty()) {
                Matcher mt = RE_NESTED_TOKEN.matcher(ks);
                while (mt.find()) {
                    keys.add(mt.group(1));
                }
            } else {
                return new String[]{ m.group(1) };
            }
        }

        return keys.toArray(new String[keys.size()]);
    };

    private NestedParams parseFromQuery(String qs) {
        HttpRequest request = new DefaultHttpRequest();
        request.setHeaders(Multimap.empty());
        request.setRequestMethod("GET");
        request.setQueryString(qs);

        new ParamsMiddleware().paramsRequest(request);
        new NestedParamsMiddleware().nestedParamsRequest(request, parseNestedKeys);

        return (NestedParams) request.getParams();
    }

    @Test
    public void test() {
        HttpRequest request = new DefaultHttpRequest();
        request.setHeaders(Multimap.empty());
        request.setRequestMethod("GET");
        request.setQueryString("intVal=123&intVal=567&doubleVal=1.7320508&decimalVal=65536" +
                "&item[name]=item1&itemList[][name]=item2&itemList[][name]=item3" +
                "&itemArray[][name]=item4&itemArray[][name]=item5");

        new ParamsMiddleware().paramsRequest(request);
        new NestedParamsMiddleware().nestedParamsRequest(request, parseNestedKeys);

        NestedForm form = createForm(NestedForm.class, request.getParams());
        assertEquals((Integer) 123, form.getIntVal());
        assertEquals((Double) 1.7320508, form.getDoubleVal());
        assertEquals(new BigDecimal("65536"), form.getDecimalVal());
        assertEquals("item1", form.getItem().getName());
        assertEquals(2, form.getItemList().size());
        assertEquals("item3", form.getItemList().get(1).getName());
    }

    @Test
    public void parseNestedQueryStringsCorrectly() {
        NestedParams p = parseFromQuery("foo=bar");
        assertEquals("bar", p.getIn("foo"));

        p = parseFromQuery("foo=\"bar\"");
        assertEquals("\"bar\"", p.getIn("foo"));

        p = parseFromQuery("foo=bar&foo=quux");
        assertEquals("bar", p.getIn("foo", 0));

        p = parseFromQuery("a=b&pid%3D1234=1023");
        assertEquals("1023", p.getIn("pid=1234"));
        assertEquals("b", p.getIn("a"));

        /*
        p = parseFromQuery("foo[]");
        assertEquals(0, ((List) p.getIn("foo")).size());
        */

        p = parseFromQuery("x[y][z]=1");
        assertEquals("1", p.getIn("x", "y", "z"));

        p = parseFromQuery("x[y][]=1");
        assertEquals("1", p.getIn("x", "y", 0));

        p = parseFromQuery("x[y][z][]=1");
        assertEquals("1", p.getIn("x", "y", "z", 0));
    }
}
