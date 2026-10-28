package krilovs.andrejs.app.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Currency;

public record AccountRequest(
        @NotNull(message = "Customer id should be defined")
        Long customerId,
        @NotNull(message = "Bank account should be defined")
        String bankAccountNumber,
        @NotNull(message = "Balance should be defined")
        @PositiveOrZero(message = "Balance should be positive or zero")
        BigDecimal balance,
        @NotNull(message = "Currency should be defined")
        Currency currency
) {
}
