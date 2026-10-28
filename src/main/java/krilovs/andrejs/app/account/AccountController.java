package krilovs.andrejs.app.account;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountController {
    private final EntityManager entityManager;
    private final AccountMapper accountMapper;

    @POST
    @Transactional(value = Transactional.TxType.REQUIRED)
    public Account createAccount(@Valid AccountRequest accountRequest) {
        var account = accountMapper.toEntity(accountRequest);
        entityManager.persist(account);
        return account;
    }
}
