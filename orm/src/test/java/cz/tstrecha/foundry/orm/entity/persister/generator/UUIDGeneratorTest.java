package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.connection.DBSession;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class UUIDGeneratorTest {

    @Test
    void providerReturnsANonNullUuid() {
        var provider = new UUIDGenerator().createValueProvider();

        assertThat(provider.apply(mock(DBSession.class)))
                .as("the whole point of the UUID generator is to hand the caller a usable id — never null, never a sentinel")
                .isNotNull()
                .isInstanceOf(UUID.class);
    }

    @Test
    void providerDoesNotTouchTheDbSession() {
        var session = mock(DBSession.class);

        new UUIDGenerator().createValueProvider().apply(session);

        verifyNoInteractions(session);
    }

    @Test
    void repeatedInvocationsReturnDistinctUuids() {
        var provider = new UUIDGenerator().createValueProvider();
        var session = mock(DBSession.class);

        var ids = IntStream.range(0, 1000)
                .mapToObj(_ -> provider.apply(session))
                .toList();

        assertThat(new HashSet<>(ids))
                .as("a UUID generator that collides inside 1000 calls is not actually random — using it as a primary key would corrupt the schema")
                .hasSameSizeAs(ids);
    }

    @Test
    void twoGeneratorInstancesStillProduceDistinctUuids() {
        var a = new UUIDGenerator().createValueProvider().apply(mock(DBSession.class));
        var b = new UUIDGenerator().createValueProvider().apply(mock(DBSession.class));

        assertThat(a)
                .as("instance identity must not steer the random stream — otherwise multiple generators in the same process would collide")
                .isNotEqualTo(b);
    }

    @Test
    void generatorIsReusableAndEachCreateValueProviderCallYieldsAFreshFunction() {
        var generator = new UUIDGenerator();
        var session = mock(DBSession.class);

        var first = generator.createValueProvider().apply(session);
        var second = generator.createValueProvider().apply(session);

        assertThat(first)
                .as("factory calls must be idempotent regarding uniqueness — reusing the same generator instance twice must still hand out two different ids")
                .isNotEqualTo(second);
    }
}
