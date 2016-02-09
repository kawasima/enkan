package enkan.component;

import enkan.application.BatchApplication;
import enkan.exception.FalteringEnvironmentException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author kawasima
 */
public class QuartzComponent extends SystemComponent {
    protected void runBatchApplication(BatchApplication batchApp) {

    }

    @Override
    protected ComponentLifecycle<QuartzComponent> lifecycle() {
        return new ComponentLifecycle<QuartzComponent>() {
            @Override
            public void start(QuartzComponent component) {
                ApplicationComponent app = getDependency(ApplicationComponent.class);
                BatchApplication batchApp = (BatchApplication) app.getApplication();
                SchedulerFactory factory = new StdSchedulerFactory();
                try {
                    Scheduler scheduler = factory.getScheduler();
                    TriggerBuilder.newTrigger().build();
                } catch (SchedulerException e) {
                    throw FalteringEnvironmentException.create(e);
                }
            }

            @Override
            public void stop(QuartzComponent component) {

            }
        };
    }
}
