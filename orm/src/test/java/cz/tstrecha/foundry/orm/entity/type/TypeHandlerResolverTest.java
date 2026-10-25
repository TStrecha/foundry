package cz.tstrecha.foundry.orm.entity.type;

import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.fixtures.Genre;
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
        assertThat(TypeHandlerResolver.findHandlerForType(String.class))
                .isInstanceOf(StringHandler.class);
    }

    @Test
    void returnsLongHandlerForLong() {
        assertThat(TypeHandlerResolver.findHandlerForType(Long.class))
                .isInstanceOf(LongHandler.class);
    }

    @Test
    void returnsEnumHandlerForEnumType() {
        assertThat(TypeHandlerResolver.findHandlerForType(Genre.class))
                .isInstanceOf(EnumHandler.class);
    }

    @Test
    void sameEnumTypeReturnsCachedInstance() {
        var a = TypeHandlerResolver.findHandlerForType(Genre.class);
        var b = TypeHandlerResolver.findHandlerForType(Genre.class);
        assertThat(a).isSameAs(b);
    }

    @Test
    void unsupportedTypeThrows() {
        assertThatThrownBy(() -> TypeHandlerResolver.findHandlerForType(FoundryConfiguration.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void primitiveLongIsResolvedToLongHandler() {
        assertThat(TypeHandlerResolver.findHandlerForType(long.class))
                .isInstanceOf(LongHandler.class);
    }

    @Test
    void integerIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Integer.class))
                .isInstanceOf(IntegerHandler.class);
    }

    @Test
    void booleanIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Boolean.class))
                .isInstanceOf(BooleanHandler.class);
    }

    @Test
    void doubleIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Double.class))
                .isInstanceOf(DoubleHandler.class);
    }

    @Test
    void bigDecimalIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(BigDecimal.class))
                .isInstanceOf(BigDecimalHandler.class);
    }

    @Test
    void uuidIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(UUID.class))
                .isInstanceOf(UUIDHandler.class);
    }

    @Test
    void instantIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(Instant.class)).isNotNull();
    }

    @Test
    void localDateIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(LocalDate.class)).isNotNull();
    }

    @Test
    void byteArrayIsSupported() {
        assertThat(TypeHandlerResolver.findHandlerForType(byte[].class))
                .isInstanceOf(ByteArrayHandler.class);
    }

    @Test
    void nullTypeIsRejected() {
        assertThatThrownBy(() -> TypeHandlerResolver.findHandlerForType(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
