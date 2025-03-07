package enkan.component;

import jakarta.transaction.TransactionManager;

/**
 * @author kawasima
 */
public abstract class TransactionComponent extends SystemComponent {
    public abstract TransactionManager getTransactionManager();
}
