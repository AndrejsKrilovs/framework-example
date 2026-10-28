package krilovs.andrejs.framework.config.transaction;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.glassfish.hk2.api.Factory;

/**
 * Provides one EntityManager per HTTP request (bound in AppBinder) and closes it when the request ends,
 * so resources just inject an EntityManager. Joining the JTA transaction is done by {@link krilovs.andrejs.framework.config.transaction.TransactionFilter}.
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RequestEntityManager implements Factory<EntityManager> {
    private final EntityManagerFactory entityManagerFactory;

    @Override
    public EntityManager provide() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public void dispose(EntityManager entityManager) {
        entityManager.close();
    }
}
