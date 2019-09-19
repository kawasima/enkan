package enkan.middleware;

import enkan.collection.Parameters;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class NestedParamsMiddlewareTest extends NestedParamsMiddleware {
    @Test
    void testParseNestedKeys() {
        Function<String, String[]> keyParser = parseNestedKeys;
        assertThat(keyParser.apply("foo[]")).containsExactly("foo", "");
        assertThat(keyParser.apply("foo[bar][][baz]"))
                .containsExactly("foo", "bar", "", "baz");
        assertThat(keyParser.apply(null))
                .isEmpty();
        assertThat(keyParser.apply(""))
                .containsExactly("");
    }

    @Test
    void testNestedParams() {
        Parameters params = Parameters.empty();
        assocNested(params,
                new String[]{"val", ""},
                new ArrayList<String>(){{ add("hoge"); }});
        assertThat(params.getIn("val", 0))
                .isEqualTo("hoge");

        params = Parameters.empty();
        assocNested(params,
                new String[]{"foo", "bar"},
                new ArrayList<String>(){{ add("baz"); }});
        assertThat(params.getIn("foo", "bar"))
                .isEqualTo("baz");

        params = Parameters.empty();
        assocNested(params,
                new String[]{"foo", "bar"},
                new ArrayList<String>(){{ add("baz"); add("bay"); }});
        assertThat(params.getIn("foo", "bar", 1))
                .isEqualTo("bay");
    }

    @Test
    void testNestedParamsRequest() {
        HttpRequest request = new DefaultHttpRequest();
        request.setParams(Parameters.of(
                "foo[aaa]", "a3",
                "foo[bbb]", "b3",
                "bar[][telNo]", "090"));
        nestedParamsRequest(request, parseNestedKeys);
        assertThat(request.getParams().getIn("bar", "0", "telNo"))
                .isEqualTo("090");
    }
}

