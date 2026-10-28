package krilovs.andrejs.app.refund;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import krilovs.andrejs.app.model.BaseEntity;
import krilovs.andrejs.app.payment.PaymentTransaction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refund_seq")
    @SequenceGenerator(name = "refund_seq", sequenceName = "refund_seq", allocationSize = 1)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private RefundStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction")
    private PaymentTransaction paymentTransaction;

    private BigDecimal amount;
    private String reason;
}
