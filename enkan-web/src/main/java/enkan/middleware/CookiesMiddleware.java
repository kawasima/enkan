package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Multimap;
import enkan.data.Cookie;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.HttpDateFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enkan.util.CodecUtils.formDecodeStr;
import static enkan.util.CodecUtils.formEncode;
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
                Cookie cookie = Cookie.create(m.group(1), formDecodeStr(stripQuotes(m.group(2))));
                cookies.put(m.group(1), cookie);
            }
        }

        return cookies;
    }

    protected String writeCookie(Cookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(formEncode(cookie.getName())).append("=").append(formEncode(cookie.getValue()));
        if (cookie.getDomain() != null) {
            sb.append(";domain=").append(cookie.getDomain());
        }
        if (cookie.getPath() != null) {
            sb.append(";path=").append(cookie.getPath());
        }
        if (cookie.getExpires() != null) {
            sb.append(";expires=").append(HttpDateFormat.RFC822.format(cookie.getExpires()));
        }
        if (cookie.getMaxAge() != null) {
            sb.append(";max-age=").append(cookie.getMaxAge());
        }
        if (cookie.isHttpOnly()) {
            sb.append(";httponly");
        }
        if (cookie.isSecure()) {
            sb.append(";secure");
        }
        return sb.toString();
    }

    protected void cookiesRequest(HttpRequest request) {
        if (request.getCookies() == null) {
            request.setCookies(parseCookies(request));
        }
    }

    protected void cookiesResponse(HttpResponse response) {
        Multimap<String, Cookie> cookieMap = response.getCookies();
        if (cookieMap != null) {
            cookieMap.keySet().forEach(key ->
                    response.getHeaders().put("Set-Cookie", writeCookie(cookieMap.get(key))));
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        cookiesRequest(request);
        HttpResponse response = castToHttpResponse(next.next(request));
        if (response != null) {
            cookiesResponse(response);
        }

        return response;
    }
}
