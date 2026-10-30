package krilovs.andrejs.app.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Currency;

public record AccountRequest(
        @NotNull(message = "Customer id should be defined")
        Long customerId,
        @NotNull(message = "Bank account should be defined")
        @NotBlank(message = "Bank account should not be blank")
        @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$", message = "Bank account should be correct")
        String bankAccountNumber,
        @NotNull(message = "Balance should be defined")
        @PositiveOrZero(message = "Balance should be positive or zero")
        @Digits(integer = 10, fraction = 2, message = "Balance must have at most 2 decimal places")
        BigDecimal balance,
        @NotNull(message = "Currency should be defined")
        Currency currency
) {
}
