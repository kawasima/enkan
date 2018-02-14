package enkan.middleware.multipart;

import enkan.collection.Parameters;
import enkan.util.ThreadingUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(content)
                .isPresent()
                .contains("contents");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testContentTypeAndNoFilename() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/contentTypeAndNoFilename");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        assertThat(params.get("text")).isEqualTo("contents");
    }

    @Test
    public void testEmpty() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/empty");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        assertThat(params.get("submit-name")).isEqualTo("Larry");
        Optional<String> content = getFileContents(params, "files");
        assertThat(content)
                .isPresent()
                .contains("");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithEncodedWords() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithEncodedWords");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertThat(content)
                .isPresent()
                .contains("contents");
        assertThat(params.getIn("files", "filename")).isEqualTo("файл");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithEscapeQuotes() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithEscapedQuotes");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertThat(content)
                .isPresent()
                .contains("contents");
        assertThat(params.getIn("files", "filename"))
                .isEqualTo("escape \"quotes");
        assertThat(params.getIn("files", "type"))
                .isEqualTo("application/octet-stream");
        assertThat(params.getIn("files", "name"))
                .isEqualTo("files");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithEscapeQuotesAndModificationParam() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithEscapedQuotesAndModificationParam");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertThat(content)
                .isPresent()
                .contains("contents");
        assertThat(params.getIn("files", "filename"))
                .isEqualTo("\"human\" genome.jpeg");
        assertThat(params.getIn("files", "type"))
                .isEqualTo("image/jpeg");
        assertThat(params.getIn("files", "name"))
                .isEqualTo("files");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithPercentEscapedQuotes() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithPercentEscapedQuotes");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertThat(content)
                .isPresent()
                .contains("contents");
        assertThat(params.getIn("files", "filename"))
                .isEqualTo("escape \"quotes");
        assertThat(params.getIn("files", "type"))
                .isEqualTo("application/octet-stream");
        assertThat(params.getIn("files", "name"))
                .isEqualTo("files");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithSingleQuote() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithSingleQuote");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=AaB03x", 0);
        Optional<String> content = getFileContents(params, "files");
        assertThat(content)
                .isPresent()
                .contains("contents");
        assertThat(params.getIn("files", "filename"))
                .isEqualTo("bob's flowers.jpg");
        Files.deleteIfExists(((File) params.getIn("files", "tempfile")).toPath());
    }

    @Test
    public void testFilenameWithUnescapedPercentages() throws IOException {
        InputStream is = getClass().getResourceAsStream("/multipart/filenameWithUnescapedPercentages");
        Parameters params = MultipartParser.parse(is, null, "multipart/form-data; boundary=----WebKitFormBoundary2NHc7OhsgU68l3Al", 0);
        Optional<String> content = getFileContents(params, "document[attachment]");
        assertThat(content)
                .isPresent()
                .contains("contents");
        assertThat(params.getIn("document[attachment]", "filename"))
                .isEqualTo("100% of a photo.jpeg");
        assertThat(params.getIn("document[attachment]", "type"))
                .isEqualTo("image/jpeg");
        assertThat(params.getIn("document[attachment]", "name"))
                .isEqualTo("document[attachment]");
        Files.deleteIfExists(((File) params.getIn("document[attachment]", "tempfile")).toPath());
    }
}
