package enkan.util;

import enkan.exception.UnrecoverableException;
import org.eclipse.collections.api.multimap.Multimap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                String chars = m.group(1);
                m.appendReplacement(sb, new String(parseBytes(chars), encoding));
            }
            m.appendTail(sb);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            UnrecoverableException.raise(e);
            return null;
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

    public static Multimap<String, String> formDecode(String encoded, String encoding) {
        MutableMultimap<String, String> m = Multimaps.mutable.list.empty();

        for(String param : encoded.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv != null && kv.length == 2) {
                m.put(formDecodeStr(kv[0], encoding),
                        formDecodeStr(kv[1], encoding));
            }
        }
        return m;
    }
}
