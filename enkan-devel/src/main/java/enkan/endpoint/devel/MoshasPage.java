package enkan.endpoint.devel;

import net.unit8.moshas.Template;
import net.unit8.moshas.context.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the page is rendered by the moshas engine.
 *
 * @author kawasima
 */
public abstract class MoshasPage {
    protected String primer;
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/css/primer.css"), StandardCharsets.ISO_8859_1))) {
            primer = reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            primer = "";
        }
    }

    abstract Template getTemplate();

    public void render(OutputStream os, Object... keyOrVals) {
        Context ctx = new Context();
        for (int i=0; i < keyOrVals.length; i+=2) {
            ctx.setVariable(Objects.toString(keyOrVals[i]), keyOrVals[i+1]);
        }
        getTemplate().render(ctx, os);
    }
}
