package enkan.middleware.multipart;

import enkan.exception.FalteringEnvironmentException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse multipart request.
 *
 * @author kawasima
 */
public class MultipartParser {
    private static final int DEFAULT_BUFFER_SIZE = 16384;

    private static final String EOL = "\r\n";
    private static final String MULTIPART_BOUNDARY = "AaB03x";
    private static final Pattern MULTIPART = Pattern.compile("multipart/.*boundary=\"?([^\";,]+)\"?");
    private static final Pattern TOKEN = Pattern.compile("[^\\s()<>,;:\\\\\"/\\[\\]?=]+");
    private static final Pattern CONDISP = Pattern.compile("Content-Disposition:\\s*" + TOKEN.pattern() + "\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALUE = Pattern.compile("\"(?:\\\\\"|[^\"])*\"|" + TOKEN.pattern());
    private static final Pattern BROKEN_QUOTED = Pattern.compile(String.format("^%s.*;\\sfilename=\"(.*?)\"(?:\\s*$|\\s*;\\s*%s=)", CONDISP, TOKEN), Pattern.CASE_INSENSITIVE);
    private static final Pattern BROKEN_UNQUOTED = Pattern.compile(String.format("^%s.*;\\sfilename=(%s)", CONDISP, TOKEN), Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTIPART_CONTENT_TYPE = Pattern.compile("Content-Type: (.*)" + EOL, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern MULTIPART_CONTENT_DISPOSITION = Pattern.compile("Content-Disposition:.*\\s+name=(" + VALUE.pattern() + ")", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern MULTIPART_CONTENT_ID = Pattern.compile("Content-ID:\\s*([^" + EOL + "]*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern ATTRIBUTE_CHAR = Pattern.compile("[^ \\t\\v\\n\\r)(><@,;:\\\\\"/\\[\\]?='*%]");
    private static final Pattern ATTRIBUTE = Pattern.compile(ATTRIBUTE_CHAR.pattern() + "+");
    private static final Pattern SECTION = Pattern.compile("\\*[0-9]+/");
    private static final Pattern REGULAR_PARAMETER_NAME = Pattern.compile(ATTRIBUTE.pattern() + SECTION.pattern() + "?");
    private static final Pattern REGULAR_PARAMETER = Pattern.compile(String.format("(%s)=(%s)", REGULAR_PARAMETER_NAME.pattern(), VALUE.pattern()));
    private static final Pattern EXTENDED_OTHER_NAME = Pattern.compile(ATTRIBUTE.pattern() + "\\*[1-9][0-9]*\\*");
    private static final Pattern EXTENDED_OTHER_VALUE = Pattern.compile("%[0-9a-fA-F]{2}|" + ATTRIBUTE_CHAR.pattern());
    private static final Pattern EXTENDED_OTHER_PARAMETER = Pattern.compile(String.format("(%s)=(%s)", EXTENDED_OTHER_NAME.pattern(), EXTENDED_OTHER_VALUE.pattern()));
    private static final Pattern EXTENDED_INITIAL_NAME = Pattern.compile(ATTRIBUTE + "(?:\\*0)?\\*");
    private static final Pattern EXTENDED_INITIAL_VALUE = Pattern.compile("[a-zA-Z0-9\\-]*'[a-zA-Z0-9\\-]*'" + EXTENDED_OTHER_VALUE.pattern() + "*");
    private static final Pattern EXTENDED_INITIAL_PARAMETER = Pattern.compile(String.format("(%s)=(%s)", EXTENDED_INITIAL_NAME.pattern(), EXTENDED_INITIAL_VALUE.pattern()));
    private static final Pattern EXTENDED_PARAMETER = Pattern.compile(EXTENDED_INITIAL_PARAMETER.pattern() + "|" + EXTENDED_OTHER_PARAMETER);
    private static final Pattern DISPPARM = Pattern.compile(String.format(";\\s*(?:%s|%s)\\s*", REGULAR_PARAMETER.pattern(), EXTENDED_PARAMETER));
    private static final Pattern RFC2183 = Pattern.compile(String.format("^%s(%s)+$", CONDISP.pattern(), DISPPARM.pattern()), Pattern.CASE_INSENSITIVE);


    private static final MultipartInfo EMPTY = new MultipartInfo(null, new ArrayList<>());
    private static final BiFunction<String, String, File> TEMPFILE_FACTORY = (filename, contentType) -> {
        int idx = filename.indexOf('.');
        String extName = (idx >= 0 && idx < filename.length()) ? filename.substring(idx + 1) : "";
        try {
            return File.createTempFile("EnkanMultipart", extName);
        } catch (IOException e) {
            throw FalteringEnvironmentException.create(e);
        }
    };


    private enum ParseState {
        FAST_FORWARD, CONSUME_TOKEN, MIME_HEAD, MIME_BODY, DONE
    }

    private enum BoundaryState {
        BOUNDARY, END_BOUNDARY, EMPTY
    }

    private ByteBuffer buf;
    private String boundary;
    private ParseState state;
    private int mimeIndex;
    private MultipartCollector collector;
    private int bufferSize;

    public MultipartParser(String boundary, int bufferSize) {
        this.bufferSize = bufferSize;
        this.boundary = "--" + boundary;
        buf = ByteBuffer.allocate(bufferSize);
        state = ParseState.FAST_FORWARD;
        collector = new MultipartCollector(TEMPFILE_FACTORY);
    }

    public void onRead(byte[] src, boolean isEOF) throws IOException {
        buf.put(src);
        runParser();
    }

    public MultipartInfo result() {
        return EMPTY; //TODO
    }

    public static String parseBoundary(String contentType) {
        if (contentType == null) return null;
        Matcher m = MULTIPART.matcher(contentType);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static MultipartInfo parse(InputStream in, long contentLength, String contentType, int bufferSize) throws IOException {
        if (contentLength == 0) return EMPTY;
        String boundary = parseBoundary(contentType);
        if (boundary == null) return EMPTY;

        if (bufferSize == 0) bufferSize = DEFAULT_BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];
        MultipartParser parser = new MultipartParser(boundary, bufferSize);
        int readed = in.read(buffer);
        parser.onRead(buffer, readed == 0);

        while (true) {
            if (parser.state == ParseState.DONE) break;
            readed = in.read(buffer, 0, parser.buf.remaining());
            parser.onRead(buffer, readed == 0);
        }

        return parser.result();
    }

    private void runParser() throws IOException {
        while (true) {
            switch (state) {
                case FAST_FORWARD:
                    if (handleFastForward()) return;
                case CONSUME_TOKEN:
                    if (handleConsumeToken()) return;
                case MIME_HEAD:
                    if (handleMimeHead()) return;
                case MIME_BODY:
                    if (handleMimeBody()) return;
                case DONE:
                    return;
            }
        }
    }

    private boolean handleFastForward() {
        if (consumeBoundary() != BoundaryState.EMPTY) {
            state = ParseState.MIME_HEAD;
            return false;
        }

        return true;
    }

    private boolean handleConsumeToken() {
        BoundaryState tok = consumeBoundary();
        if (tok == BoundaryState.END_BOUNDARY || tok == BoundaryState.EMPTY) {
            state = ParseState.DONE;
        } else {
            state = ParseState.MIME_HEAD;
        }

        return false;
    }

    private boolean handleMimeHead() throws IOException {
        buf.mark();
        while (buf.hasRemaining()) {
            if (buf.get() == '\r'
                    && buf.hasRemaining() && buf.get() == '\n'
                    && buf.hasRemaining() && buf.get() == '\r'
                    && buf.hasRemaining() && buf.get() == '\n') {
                int end = buf.position() - 4;
                buf.reset();
                int start = buf.position();
                byte[] headBuf = new byte[end - start + 1];
                buf.get(headBuf);
                String head = new String(headBuf, StandardCharsets.UTF_8);
                // Read two CR+LF.
                for (int i=0; i<4; i++) buf.get();
                buf.compact();

                String contentType = null;
                Matcher contentTypeMatcher = MULTIPART_CONTENT_TYPE.matcher(head);
                if (contentTypeMatcher.find()) {
                    contentType = contentTypeMatcher.group(1);
                }

                String name = null;
                Matcher contentDispositionMatcher = MULTIPART_CONTENT_DISPOSITION.matcher(head);
                if (contentDispositionMatcher.find()) {
                    name = contentDispositionMatcher.group(1);
                } else {
                    Matcher contentIdMatcher = MULTIPART_CONTENT_ID.matcher(head);
                    if (contentIdMatcher.find()) {
                        name = contentIdMatcher.group(1);
                    }
                }

                String filename = getFilename(name);

                collector.onMimeHead(mimeIndex, head, filename, contentType, name);
                state = ParseState.MIME_BODY;
                return false;
            }
        }
        return true;
    }

    private boolean handleMimeBody() {
        return true;
    }

    private BoundaryState consumeBoundary() {
        buf.flip();
        while (true) {
            while (buf.hasRemaining() && buf.get() == '\n');
            if (buf.hasRemaining()) {
                buf.position(buf.position() - 1);
            }
            buf.mark();
            while (buf.hasRemaining()) {
                byte b = buf.get();
                if (b == '\n') break;
            }
            int end = buf.position() - 1;
            buf.reset();

            byte[] boundaryBuf = new byte[end - buf.position()];
            buf.get(boundaryBuf);
            String readedBoundary = new String(boundaryBuf, StandardCharsets.UTF_8).trim();
            buf.compact();
            if (readedBoundary.equals(boundary)) {
                return BoundaryState.BOUNDARY;
            } else if (readedBoundary.equals(boundary + "--")) {
                return BoundaryState.END_BOUNDARY;
            } else if (!buf.hasRemaining()) {
                return BoundaryState.EMPTY;
            }
        }
    }

    private String getFilename(String head) {
        String filename = null;
        Matcher rfc2183Matcher = RFC2183.matcher(head);
        Matcher brokenQuotedMatcher = BROKEN_QUOTED.matcher(head);
        Matcher brokenUnQuotedMatcher = BROKEN_UNQUOTED.matcher(head);

        if (rfc2183Matcher.find()) {
            Map<String, String> params = new HashMap<>();
            Matcher disparmMatchr = DISPPARM.matcher(head);
            if (disparmMatchr.find()) {
                params.put(disparmMatchr.group(1), disparmMatchr.group(2));
            }

            if (params.containsKey("filename")) {
                filename = params.get("filename");
                filename.replaceAll("^\"(.*)\"$", "$1");
            } else if (params.containsKey("filename*")) {
                String[] tokens = params.get("filename*").split("'", 3);
                filename = tokens[2];
            }
        } else if (brokenQuotedMatcher.find()) {
            filename = brokenQuotedMatcher.group(1);
        } else if (brokenUnQuotedMatcher.find()) {
            filename = brokenUnQuotedMatcher.group(1);
        }

        if (filename == null) return null;

        return null;
    }

    private void tagMultipartEncoding(String filename, String contentType, String name, InputStream body) {
        if (filename != null) return;

        if (contentType != null) {
            String[] list = contentType.split(";");
            String typeSubtype = list[0].trim();

            if (typeSubtype.equals("text/plain")) {
            }
        }
    }
}
