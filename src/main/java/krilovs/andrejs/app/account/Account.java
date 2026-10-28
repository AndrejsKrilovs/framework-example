package krilovs.andrejs.app.account;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import krilovs.andrejs.app.model.CurrencyConverter;
import krilovs.andrejs.app.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;

@Getter
@Setter
@Entity
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_seq", allocationSize = 1)
    private Long id;

    @Convert(converter = CurrencyConverter.class)
    private Currency currency;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    private BigDecimal balance;
}
