package krilovs.andrejs.framework.config.transaction;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import krilovs.andrejs.framework.config.PropertiesConfig;
import lombok.experimental.UtilityClass;

/**
 * The Narayana transaction manager, standalone (no application server, no CDI).
 * Narayana reads its settings from system properties named {@code <Bean>.<property>} the first
 * time it is used, so they are set here before the first call.
 */
@UtilityClass
public class TransactionConfig {

    public static TransactionManager transactionManager() {
        return Holder.INSTANCE;
    }

    public static TransactionSynchronizationRegistry transactionSynchronizationRegistry() {
        return Holder.REGISTRY;
    }

    private static class Holder {
        static final TransactionManager INSTANCE = create();
        static final TransactionSynchronizationRegistry REGISTRY =
                jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();

        private static TransactionManager create() {
            System.getProperties().putIfAbsent("CoreEnvironmentBean.nodeIdentifier",
                    PropertiesConfig.get("transaction.node_identifier"));
            System.getProperties().putIfAbsent("ObjectStoreEnvironmentBean.objectStoreDir",
                    PropertiesConfig.get("transaction.object_store_dir"));
            return com.arjuna.ats.jta.TransactionManager.transactionManager();
        }
    }
}
