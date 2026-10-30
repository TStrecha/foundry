package cz.tstrecha.foundry.orm.entity.type;

import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.Genre;
import jakarta.persistence.EnumType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeHandlerResolverTest {

    @Test
    void returnsStringHandlerForString() {
        assertThat(TypeHandlerResolver.findHandlerForType(String.class, null))
                .isInstanceOf(StringHandler.class);
    }

    @Test
    void returnsLongHandlerForLong() {
        assertThat(TypeHandlerResolver.findHandlerForType(Long.class, null))
                .isInstanceOf(LongHandler.class);
    }

    @Test
    void returnsStringEnumHandlerForEnumWithStringType() {
        assertThat(TypeHandlerResolver.findHandlerForType(Genre.class, EnumType.STRING))
                .isInstanceOf(EnumStringHandler.class);
    }

    @Test
    void sameEnumTypeReturnsCachedInstance() {
        var a = TypeHandlerResolver.findHandlerForType(Genre.class, EnumType.STRING);
        var b = TypeHandlerResolver.findHandlerForType(Genre.class, EnumType.STRING);
        assertThat(a).isSameAs(b);
    }

    @Test
    void nullEnumTypeIsRejectedForEnumClass() {
        enum StandaloneEnum { A }
        assertThatThrownBy(() -> TypeHandlerResolver.findHandlerForType(StandaloneEnum.class, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unsupportedTypeThrows() {
        assertThatThrownBy(() -> TypeHandlerResolver.findHandlerForType(FoundryConfiguration.class, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void primitiveLongIsResolvedToLongHandler() {
        assertThat(TypeHandlerResolver.findHandlerForType(long.class, null))
                .isInstanceOf(LongHandler.class);
    }

    @Test
    void integerIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Integer.class, null))
                .isInstanceOf(IntegerHandler.class);
    }

    @Test
    void booleanIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Boolean.class, null))
                .isInstanceOf(BooleanHandler.class);
    }

    @Test
    void doubleIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Double.class, null))
                .isInstanceOf(DoubleHandler.class);
    }

    @Test
    void bigDecimalIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(BigDecimal.class, null))
                .isInstanceOf(BigDecimalHandler.class);
    }

    @Test
    void uuidIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(UUID.class, null))
                .isInstanceOf(UUIDHandler.class);
    }

    @Test
    void instantIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Instant.class, null))
                .isInstanceOf(InstantHandler.class);
    }

    @Test
    void localDateIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(LocalDate.class, null))
                .isInstanceOf(LocalDateHandler.class);
    }

    @Test
    void byteArrayIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(byte[].class, null))
                .isInstanceOf(ByteArrayHandler.class);
    }

    @Test
    void nullTypeIsRejected() {
        assertThatThrownBy(() -> TypeHandlerResolver.findHandlerForType(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private enum StringFirstEnum { A, B }

    private enum OrdinalFirstEnum { X, Y }

    @Test
    void sameEnumClassWithStringThenOrdinalReturnsDistinctHandlers() {
        var string = TypeHandlerResolver.findHandlerForType(StringFirstEnum.class, EnumType.STRING);
        var ordinal = TypeHandlerResolver.findHandlerForType(StringFirstEnum.class, EnumType.ORDINAL);

        assertThat(string)
                .as("the first STRING call resolves to the string handler")
                .isInstanceOf(EnumStringHandler.class);
        assertThat(ordinal)
                .as("a subsequent ORDINAL call for the SAME enum class must resolve to the ordinal handler — the cache cannot pin the handler to the class alone, it must key by (class, enumType), otherwise two fields of the same enum type with different @Enumerated modes silently share the first-call handler")
                .isInstanceOf(EnumOrdinalHandler.class);
    }

    @Test
    void sameEnumClassWithOrdinalThenStringReturnsDistinctHandlers() {
        var ordinal = TypeHandlerResolver.findHandlerForType(OrdinalFirstEnum.class, EnumType.ORDINAL);
        var string = TypeHandlerResolver.findHandlerForType(OrdinalFirstEnum.class, EnumType.STRING);

        assertThat(ordinal)
                .as("the first ORDINAL call resolves to the ordinal handler")
                .isInstanceOf(EnumOrdinalHandler.class);
        assertThat(string)
                .as("a subsequent STRING call for the SAME enum class must resolve to the string handler — the cache cannot pin the handler to the class alone, it must key by (class, enumType)")
                .isInstanceOf(EnumStringHandler.class);
    }
}
