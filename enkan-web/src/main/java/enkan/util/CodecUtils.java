package enkan.util;

import enkan.collection.Parameters;
import enkan.exception.MisconfigurationException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The utilities for codec.
 *
 * @author kawasima
 */
public class CodecUtils {
    private static final Pattern RE_URL_ENCODED_CHARS = Pattern.compile("(?:%[A-Za-z0-9]{2})+");
    private static final Pattern RE_URL_ENCODED_CHAR = Pattern.compile("%[A-Za-z0-9]{2}");
    private static final Pattern RE_URL_ENCODE_TARGET = Pattern.compile("[^A-Za-z0-9_~\\.+\\-]+");

    public static byte[] parseBytes(String encodedBytes) {
        List<Byte> bytes = new ArrayList<>();
        Matcher m = RE_URL_ENCODED_CHAR.matcher(encodedBytes);

        while(m.find()) {
            bytes.add(Integer.valueOf(m.group(0).substring(1), 16).byteValue());
        }
        int len = bytes.size();
        byte[] ret = new byte[len];
        for(int i = 0; i<len; i++) {
            ret[i] = bytes.get(i);
        }
        return ret;
    }

    public static String urlEncode(String unencoded) {
        return urlEncode(unencoded, "UTF-8");
    }

    public static String urlEncode(String unencoded, String encoding) {
        try {
            Matcher m = RE_URL_ENCODE_TARGET.matcher(unencoded);

            StringBuffer sb = new StringBuffer(unencoded.length() * 2);
            while (m.find()) {
                String s = m.group(0);
                StringBuilder encodedSb = new StringBuilder();
                for (byte b : s.getBytes(encoding)) {
                    encodedSb.append("%");
                    int d = (int) b;
                    if (d < 0) {
                        d += 256;
                    }
                    if (d < 16) {
                        encodedSb.append("0");
                    }
                    encodedSb.append(Integer.toString(d, 16));
                }
                m.appendReplacement(sb, encodedSb.toString());
            }
            m.appendTail(sb);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return unencoded;
        }
    }

    public static String urlDecode(String encoded) {
        return urlDecode(encoded, "UTF-8");
    }

    public static String urlDecode(String encoded, String encoding) {
        try {
            Matcher m = RE_URL_ENCODED_CHARS.matcher(encoded);
            StringBuffer sb = new StringBuffer(encoded.length());
            while (m.find()) {
                String chars = m.group(0);
                m.appendReplacement(sb, new String(parseBytes(chars), encoding));
            }
            m.appendTail(sb);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw MisconfigurationException.create("UNSUPPORTED_ENCODING", encoding, e);
        }
    }

    public static <T> String formEncode(T x) {
        return formEncode(x, "UTF-8");
    }

    public static <T> String formEncode(T x, String encoding) {
        if (x == null) {
            return null;
        } else if (x instanceof String) {
            try {
                return URLEncoder.encode((String) x, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(String.format("encoding %s is not supported", x), e);
            }
        } else if (x instanceof Map) {
            Map<?, ?> m = (Map) x;
            return m.entrySet().stream()
                    .map(e -> formEncode(e.getKey()) + "=" + formEncode(e.getValue()))
                    .collect(Collectors.joining("&"));
        } else {
            return formEncode(x.toString(), encoding);
        }
    }

    public static String formDecodeStr(String encoded) {
        return formDecodeStr(encoded, "UTF-8");
    }

    public static String formDecodeStr(String encoded, String encoding) {
        try {
            return URLDecoder.decode(encoded, encoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static Parameters formDecode(String encoded, String encoding) {
        Parameters m = Parameters.empty();

        for(String param : encoded.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 1) {
                m.put(formDecodeStr(kv[0], encoding), null);
            } else if (kv.length == 2) {
                m.put(formDecodeStr(kv[0], encoding),
                        formDecodeStr(kv[1], encoding));
            }
        }
        return m;
    }
}
