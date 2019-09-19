package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Multimap;
import enkan.data.Cookie;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enkan.util.CodecUtils.*;
import static enkan.util.ParsingUtils.*;

/**
 * Middleware for parsing or formatting http cookies.
 *
 * @author kawasima
 */
@Middleware(name = "cookies")
public class CookiesMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private static final Pattern RE_COOKIE_OCTET = Pattern.compile("[!#$%&'()*+\\-./0-9:<=>?@A-Z\\[\\]\\^_`a-z\\{\\|\\}~]");
    private static final Pattern RE_COOKIE_VALUE = Pattern.compile("\"" + RE_COOKIE_OCTET.pattern() +  "*\"|" + RE_COOKIE_OCTET.pattern() + "*");
    private static final Pattern RE_COOKIE = Pattern.compile("\\s*(" + RE_TOKEN + ")=(" + RE_COOKIE_VALUE.pattern() + ")\\s*[;,]?");

    /**
     * Strip quotes from argument string.
     *
     * @param value a String contains quote character
     * @return a stripped string
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
                Cookie cookie = Cookie.create(m.group(1), formDecodeStr(stripQuotes(m.group(2))));
                cookies.put(m.group(1), cookie);
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
        Multimap<String, Cookie> cookieMap = response.getCookies();
        if (cookieMap != null) {
            cookieMap.values().forEach(cookie ->
                    response.getHeaders().put("Set-Cookie", cookie.toHttpString()));
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> next) {
        cookiesRequest(request);
        HttpResponse response = castToHttpResponse(next.next(request));
        if (response != null) {
            cookiesResponse(response);
        }

        return response;
    }
}
