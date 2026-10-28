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
        assertThat(TypeHandlerResolver.findHandlerForType(Instant.class, null)).isNotNull();
    }

    @Test
    void localDateIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(LocalDate.class, null)).isNotNull();
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
}
