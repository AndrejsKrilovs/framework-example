package krilovs.andrejs.app.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Currency;
import java.util.Objects;

/** Stores java.util.Currency as its ISO 4217 code; EclipseLink would otherwise serialize it to bytes. */
@Converter
public class CurrencyConverter implements AttributeConverter<Currency, String> {
    @Override
    public String convertToDatabaseColumn(Currency currency) {
        return Objects.isNull(currency) ? null : currency.getCurrencyCode();
    }

    @Override
    public Currency convertToEntityAttribute(String code) {
        return Objects.isNull(code) ? null : Currency.getInstance(code);
    }
}
