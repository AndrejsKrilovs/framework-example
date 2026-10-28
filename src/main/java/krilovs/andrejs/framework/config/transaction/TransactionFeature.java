package krilovs.andrejs.framework.config.transaction;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

/**
 * Gives {@link jakarta.transaction.Transactional} its meaning in Jersey: there is no CDI container or JTA here to intercept it,
 * so the {@link krilovs.andrejs.framework.config.transaction.TransactionFilter} is attached to every resource method annotated with it
 * (on the method or its class).
 * <p>
 * Only the basic behaviour is implemented: one resource-local transaction per request.
 * {@code value} (TxType), {@code rollbackOn} and {@code dontRollbackOn} are ignored;
 * a response with status 400 or above rolls back.
 */
@Provider
public class TransactionFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(Transactional.class)
                || resourceInfo.getResourceClass().isAnnotationPresent(Transactional.class)) {
            context.register(TransactionFilter.class);
        }
    }
}
