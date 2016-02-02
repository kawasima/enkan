package enkan.component;

import javax.transaction.TransactionManager;

/**
 * @author kawasima
 */
public abstract class TransactionComponent extends SystemComponent {
    public abstract TransactionManager getTransactionManager();
}
