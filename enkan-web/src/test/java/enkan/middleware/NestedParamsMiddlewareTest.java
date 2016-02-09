package enkan.middleware;

import enkan.collection.Parameters;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author kawasima
 */
public class NestedParamsMiddlewareTest extends NestedParamsMiddleware {
    @Test
    public void testParseNestedKeys() {
        Function<String, String[]> keyParser = parseNestedKeys;
        assertArrayEquals(new String[]{"foo", ""}, keyParser.apply("foo[]"));
        assertArrayEquals(new String[]{"foo", "bar", "", "baz"}, keyParser.apply("foo[bar][][baz]"));
        assertArrayEquals(new String[]{}, keyParser.apply(null));
        assertArrayEquals(new String[]{""}, keyParser.apply(""));
    }

    @Test
    public void testNestedParams() {
        Parameters params = Parameters.empty();
        assocNested(params,
                new String[]{"val", ""},
                new ArrayList<String>(){{ add("hoge"); }});
        assertEquals("hoge", params.getIn("val", 0));


        params = Parameters.empty();
        assocNested(params,
                new String[]{"foo", "bar"},
                new ArrayList<String>(){{ add("baz"); }});
        assertEquals("baz", params.getIn("foo", "bar"));

        params = Parameters.empty();
        assocNested(params,
                new String[]{"foo", "bar"},
                new ArrayList<String>(){{ add("baz"); add("bay"); }});
        assertEquals("bay", params.getIn("foo", "bar", 1));
    }

    @Test
    public void testNestedParamsRequest() {
        HttpRequest request = new DefaultHttpRequest();
        request.setParams(Parameters.of(
                "foo[aaa]", "a3",
                "foo[bbb]", "b3",
                "bar[][telNo]", "090"));
        nestedParamsRequest(request, parseNestedKeys);
        assertEquals("090", request.getParams().getIn("bar", "0", "telNo"));
    }
}

