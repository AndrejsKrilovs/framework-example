package krilovs.andrejs.framework.config.transaction;

import jakarta.transaction.TransactionManager;
import org.eclipse.persistence.transaction.JTATransactionController;

/**
 * Tells EclipseLink which JTA transaction manager to use (passed as eclipselink.target-server).
 * Instantiated by EclipseLink from the class name, so it needs a public no-arg constructor.
 */
public class NarayanaTransactionController extends JTATransactionController {
    @Override
    protected TransactionManager acquireTransactionManager() {
        return TransactionConfig.transactionManager();
    }
}
