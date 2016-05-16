package enkan.middleware.multipart;

import enkan.collection.Parameters;
import enkan.util.ThreadingUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class MultipartParserTest {
    private Optional<String> getFileContents(Parameters params, String parameterName) {
        return ThreadingUtils.some(((File) params.getIn(parameterName, "tempfile")).toPath(),
                Files::readAllLines,
                slist -> String.join("", slist));
    }

    @Test
    public void testIE() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/ie");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testContentTypeAndNoFilename() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/contentTypeAndNoFilename");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        assertEquals("contents", params.get("text"));
    }

    @Test
    public void testEmpty() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/empty");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        assertEquals("Larry", params.get("submit-name"));
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("", content.get());
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithEncodedWords() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithEncodedWords");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        assertEquals("файл", params.getIn("files", "filename"));
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithEscapeQuotes() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithEscapedQuotes");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        assertEquals("escape \"quotes", params.getIn("files", "filename"));
        assertEquals("application/octet-stream", params.getIn("files", "type"));
        assertEquals("files", params.getIn("files", "name"));
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithEscapeQuotesAndModificationParam() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithEscapedQuotesAndModificationParam");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        assertEquals("\"human\" genome.jpeg", params.getIn("files", "filename"));
        assertEquals("image/jpeg", params.getIn("files", "type"));
        assertEquals("files", params.getIn("files", "name"));
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithPercentEscapedQuotes() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithPercentEscapedQuotes");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        assertEquals("escape \"quotes", params.getIn("files", "filename"));
        assertEquals("application/octet-stream", params.getIn("files", "type"));
        assertEquals("files", params.getIn("files", "name"));
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithSingleQuote() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithSingleQuote");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        assertEquals("bob's flowers.jpg", params.getIn("files", "filename"));
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithUnescapedPercentages() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithUnescapedPercentages");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=----WebKitFormBoundary2NHc7OhsgU68l3Al", 0);
        Optional<String> content = getFileContents(params, "document[attachment]");
        assertTrue(content.isPresent());
        assertEquals("contents", content.get());
        assertEquals("100% of a photo.jpeg", params.getIn("document[attachment]", "filename"));
        assertEquals("image/jpeg", params.getIn("document[attachment]", "type"));
        assertEquals("document[attachment]", params.getIn("document[attachment]", "name"));
        Files.deleteIfExists(((File) params.getIn("document[attachment]", "tempfile")).toPath());
    }


    @Test
    public void testRegexp() {
        Matcher m;

        m = MultipartParser.REGULAR_PARAMETER_NAME.matcher("filename");
        assertTrue(m.matches());
        m = MultipartParser.RFC2183.matcher("Content-Disposition: form-data;  filename=\"C:\\Documents and Settings\\Administrator\\Desktop\\file1.txt\"");
        assertTrue(m.find());
    }
}
