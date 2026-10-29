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
        System.getProperties().putIfAbsent(
                "CoreEnvironmentBean.nodeIdentifier",
                PropertiesConfig.get("database.transaction.node_identifier")
        );
        System.getProperties().putIfAbsent(
                "ObjectStoreEnvironmentBean.objectStoreDir",
                PropertiesConfig.get("database.transaction.object_store_dir")
        );
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    public static TransactionSynchronizationRegistry transactionSynchronizationRegistry() {
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();
    }
}
