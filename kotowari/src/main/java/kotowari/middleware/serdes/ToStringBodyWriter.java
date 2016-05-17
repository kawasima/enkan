package kotowari.middleware.serdes;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author kawasima
 */
public class ToStringBodyWriter implements MessageBodyWriter<Object> {
    private enum StringBodyConverter {
        PLAIN("%s", s -> s),
        XML("<message>%s</message>", ToStringBodyWriter::escapeXml),
        HTML("<html><body>%s</body></html>", ToStringBodyWriter::escapeHtml);


        StringBodyConverter(String format, Function<String, String> escaper) {
            this.format = format;
            this.escaper = escaper;
        }

        public String convert(Object o) {
            return String.format(Locale.US, format, escaper.apply(Objects.toString(o, "")));
        }

        public static StringBodyConverter valueOfOrDefault(String name) {
            return Arrays.stream(StringBodyConverter.values())
                    .filter(c -> c.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(StringBodyConverter.PLAIN);
        }

        private String format;
        private Function<String, String> escaper;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Objects.equals(mediaType.getType(), "text") || mediaType.isWildcardType();
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        StringBodyConverter converter = StringBodyConverter.valueOfOrDefault(mediaType.getSubtype());
        entityStream.write(converter.convert(o).getBytes("UTF-8"));
    }

    // FIXME It's not efficient. But I don't wanna use commons-lang just for this.
    private static String escapeHtml(String s) {
        return s.replaceAll("\"", "&quot;")
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private static String escapeXml(String s) {
        return escapeHtml(s)
                .replace("\'", "&apos;")
                .replaceAll("[\u0000-\u001f\ufffe\uffff]+", "");
    }

}
