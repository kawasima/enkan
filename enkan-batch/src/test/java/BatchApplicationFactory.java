import enkan.Application;
import enkan.application.BatchApplication;
import enkan.batch.reader.FileItemReader;
import enkan.config.ApplicationFactory;
import enkan.endpoint.ChunkedStep;
import enkan.system.inject.ComponentInjector;

import java.io.File;

/**
 * @author kawasima
 */
public class BatchApplicationFactory implements ApplicationFactory {
    @Override
    public Application create(ComponentInjector injector) {
        BatchApplication batch = new BatchApplication();

        batch.use(new ChunkedStep(
                new FileItemReader<>(new File(""), line -> {

                }),
                null,

                ));
        return null;
    }
}
