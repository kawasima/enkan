package kotowari.routing;

import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import static enkan.util.HttpResponseUtils.RedirectStatusCode.SEE_OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class UrlRewriterTest {
    @Test
    void parseOptionString() {
        assertThat(UrlRewriter.parseOptionString(FooController.class, "index?a=1&b=2"))
                .contains(entry("a", "1"))
                .contains(entry("b", "2"));
    }

    @Test
    void redirect() {
        assertThat(UrlRewriter.redirect(FooController.class, "index?c=3", SEE_OTHER))
                .is(new Condition<>(response ->
                        response.getStatus() == 303
                                && response.getHeaders().containsKey("Location"),
                        "Have a location header"));
    }

    @Test
    void urlFor() {
        assertThat(UrlRewriter.urlFor(FooController.class, "index?d=4").getOptions())
                .contains(entry("d", "4"))
                .contains(entry("controller", FooController.class));
    }

    @Test
    void urlForWithControllerParameter() {
        assertThat(UrlRewriter.urlFor(FooController.class, "index?controller=Bar").getOptions())
                .contains(entry("controller", FooController.class));
    }

    @Test
    void urlForWithOptionMap() {
        assertThat(UrlRewriter.urlFor(OptionMap.of("e", "4")).getOptions())
                .contains(entry("e", "4"));
    }

    private static class FooController {
        public HttpResponse index(HttpRequest request) {
            return null;
        }
    }
}