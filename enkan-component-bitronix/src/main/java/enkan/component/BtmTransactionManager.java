package enkan.component;

/**
 * @author kawasima
 */
public class BtmTransactionManager extends SystemComponent {

    @Override
    protected ComponentLifecycle<BtmTransactionManager> lifecycle() {
        return new ComponentLifecycle<BtmTransactionManager>() {
            @Override
            public void start(BtmTransactionManager component) {

            }

            @Override
            public void stop(BtmTransactionManager component) {

            }
        };
    }
}
