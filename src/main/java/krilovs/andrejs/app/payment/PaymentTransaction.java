package krilovs.andrejs.app.payment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import krilovs.andrejs.app.account.Account;
import krilovs.andrejs.app.model.BaseEntity;
import krilovs.andrejs.app.model.CurrencyConverter;
import krilovs.andrejs.app.refund.Refund;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_transaction_seq")
    @SequenceGenerator(name = "payment_transaction_seq", sequenceName = "payment_transaction_seq", allocationSize = 1)
    private Long id;

    @Convert(converter = CurrencyConverter.class)
    private Currency currency;

    @Enumerated(value = EnumType.STRING)
    private PaymentTransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account")
    private Account destinationAccount;

    @Column(name = "error_message")
    private String errorMessage;

    @OneToMany(mappedBy = "paymentTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds;

    private BigDecimal amount;
}
