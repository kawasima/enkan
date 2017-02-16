package enkan.system.devel.compiler;

import enkan.Env;
import enkan.exception.FalteringEnvironmentException;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.devel.CompileResult;
import enkan.system.devel.Compiler;
import org.gradle.tooling.*;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author kawasima
 */
public class GradleCompiler implements Compiler {
    private static Logger LOG = LoggerFactory.getLogger(GradleCompiler.class);

    private String projectDirectory = ".";
    private String gradleVersion;

    @Override
    public CompileResult execute(Transport t) {
        GradleConnector c = GradleConnector.newConnector();
        c.useGradleVersion(Optional.ofNullable(gradleVersion).orElse(GradleVersion.current().getVersion()))
                .forProjectDirectory(new File(projectDirectory));

        String gradleHome = Env.get("GRADLE_HOME");
        c.useInstallation(new File(gradleHome));
        ProjectConnection connection = c.connect();

        BuildLauncher launcher = connection.newBuild();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        launcher.setStandardError(err);
        launcher.setStandardOutput(out);
        launcher.forTasks("compileJava");
        CompletableFuture<GradleConnectionException> future = new CompletableFuture<>();
        launcher.run(new ResultHandler<Void>() {
            @Override
            public void onComplete(Void result) {
                LOG.info("gradle execution complete");
                future.complete(null);
            }

            @Override
            public void onFailure(GradleConnectionException failure) {
                future.complete(failure);
            }
        });

        try {
            GradleConnectionException gce = future.get();
            if (out.size() > 0) {
                t.send(ReplResponse.withOut(new String(out.toByteArray())));
            }
            if (err.size() > 0) {
                t.send(ReplResponse.withErr(new String(err.toByteArray())));
            }
            CompileResult result = new CompileResult();
            result.setExecutionException(gce);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new FalteringEnvironmentException(e);
        }
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public void setGradleVersion(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }
}
