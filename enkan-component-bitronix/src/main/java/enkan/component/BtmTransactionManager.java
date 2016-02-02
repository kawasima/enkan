package enkan.component;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

import javax.transaction.TransactionManager;

/**
 * @author kawasima
 */
public class BtmTransactionManager extends TransactionComponent {
    private BitronixTransactionManager transactionManager;

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    protected ComponentLifecycle<BtmTransactionManager> lifecycle() {
        return new ComponentLifecycle<BtmTransactionManager>() {
            @Override
            public void start(BtmTransactionManager component) {
                component.transactionManager = TransactionManagerServices.getTransactionManager();
            }

            @Override
            public void stop(BtmTransactionManager component) {
                if (component.transactionManager != null) {
                    component.transactionManager.shutdown();
                    component.transactionManager = null;
                }
            }
        };
    }
}
