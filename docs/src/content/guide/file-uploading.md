type=page
status=published
title=File uploading | Enkan
~~~~~~

# File uploading

Enkan provides the middleware for handling multipart requests.
Most Java web framework depends on commons-fileupload, but Enkan is not dependent on it.

Using `MultipartParamsMiddleware` a multipart request is processed by it and puts the parsed file into a temporary directory. 

You can access the temporary file as follows:

```language-java
public class UploadController {
    public String upload(Parameters params) {
        File tempfile = (File) params.getIn("datafile", "tempfile");
        return tempfile.getAbsolutePath() + "("
                + tempfile.length()
                + " bytes) is uploaded. description: "
                + params.get("description");
    }
}
```
