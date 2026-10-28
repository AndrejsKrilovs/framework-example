package krilovs.andrejs.framework.config.transaction;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import lombok.RequiredArgsConstructor;

/**
 * JTA transaction boundary for resources annotated with {@link jakarta.transaction.Transactional}.
 * Not a @Provider on purpose: {@link krilovs.andrejs.framework.config.transaction.TransactionFeature} registers it only for the annotated methods.
 * <p>
 * The response filter also runs for responses built by exception mappers, so a failed request is rolled back.
 * It is idempotent: if the commit fails and the error is mapped, the second pass finds no transaction.
 * The EntityManager is flushed explicitly before the commit: a failure inside the transaction manager's
 * completion callbacks would reach us as a bare "rolled back", while a failed flush carries the database error.
 * <p>
 * The filter is a singleton, so it asks for the request-scoped EntityManager through a Provider.
 * The request and response filters run on the same thread, which is what ties the transaction to the request.
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TransactionFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final int FIRST_ERROR_STATUS = 400;

    private final Provider<EntityManager> entityManager;
    private final TransactionManager transactionManager;

    @Override
    public void filter(ContainerRequestContext request) {
        try {
            // a transaction left on this pooled thread by a request that never finished must not leak into this one
            if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                transactionManager.rollback();
            }
            transactionManager.begin();
            entityManager.get().joinTransaction();
        }
        catch (Exception e) {
            throw new PersistenceException("Cannot begin the transaction", e);
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            var status = transactionManager.getStatus();
            if (status == Status.STATUS_NO_TRANSACTION) {
                return;
            }
            if (status == Status.STATUS_ACTIVE && response.getStatus() < FIRST_ERROR_STATUS) {
                flushOrRollBack();
                transactionManager.commit();
            }
            else {
                transactionManager.rollback();
            }
        }
        catch (PersistenceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PersistenceException("Cannot complete the transaction", e);
        }
    }

    private void flushOrRollBack() throws Exception {
        try {
            entityManager.get().flush();
        }
        catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
