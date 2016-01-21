package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.Cookie;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enkan.util.CodecUtils.formDecodeStr;
import static enkan.util.ParsingUtils.RE_TOKEN;

/**
 * Middleware for parsing or formatting http cookies.
 *
 * @author kawasima
 */
@Middleware(name = "cookies")
public class CookiesMiddleware extends AbstractWebMiddleware {
    private static final Pattern RE_COOKIE_OCTET = Pattern.compile("[!#$%&'()*+\\-./0-9:<=>?@A-Z\\[\\]\\^_`a-z\\{\\|\\}~]");
    private static final Pattern RE_COOKIE_VALUE = Pattern.compile("\"" + RE_COOKIE_OCTET.pattern() +  "*\"|" + RE_COOKIE_OCTET.pattern() + "*");
    private static final Pattern RE_COOKIE = Pattern.compile("\\s*(" + RE_TOKEN + ")=(" + RE_COOKIE_VALUE.pattern() + ")\\s*[;,]?");

    /**
     * Strip quotes from argument string.
     *
     * @param value
     * @return stripped string
     */
    protected String stripQuotes(String value) {
        return value.replaceAll("^\"|\"$", "");
    }

    protected Map<String, Cookie> parseCookies(HttpRequest request) {
        String cookieHeader = request.getHeaders().get("cookie");
        Map<String, Cookie> cookies = new HashMap<>();

        if (cookieHeader != null) {
            Matcher m = RE_COOKIE.matcher(cookieHeader);
            while (m.find()) {
                Cookie cookie = Cookie.create(m.group(2), formDecodeStr(stripQuotes(m.group(3))));
                cookies.put(m.group(2), cookie);
            }
        }

        return cookies;
    }

    protected void cookiesRequest(HttpRequest request) {
        if (request.getCookies() == null) {
            request.setCookies(parseCookies(request));
        }
    }

    protected void cookiesResponse(HttpResponse response) {

    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest, MiddlewareChain next) {
        return null;
    }
}
