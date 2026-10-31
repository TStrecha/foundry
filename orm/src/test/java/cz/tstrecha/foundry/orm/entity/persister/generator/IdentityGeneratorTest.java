package cz.tstrecha.foundry.orm.entity.persister.generator;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IdentityGeneratorTest {

    @Test
    void isAPostInsertValueGenerator() {
        assertThat(new IdentityGenerator())
                .as("IDENTITY values are produced by the database during INSERT and extracted from RETURNING — the generator must opt into the post-insert branch of the pipeline, otherwise the persist flow treats it as pre-insert and tries to bind null")
                .isInstanceOf(PostInsertValueGenerator.class);
    }

    @Test
    void consumerReadsTheValueFromTheSuppliedColumnIndex() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.getLong(3)).thenReturn(42L);

        var value = new IdentityGenerator().createValueConsumer().consume(resultSet, 3);

        assertThat(value)
                .as("the consumer must read exactly the column it was asked to — mis-indexing would pull another column's value and silently assign the wrong id")
                .isEqualTo(42L);
        verify(resultSet).getLong(3);
    }

    @Test
    void consumerReturnsZeroWhenResultSetReturnsZero() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.getLong(1)).thenReturn(0L);

        var value = new IdentityGenerator().createValueConsumer().consume(resultSet, 1);

        assertThat(value)
                .as("the generator is a thin extractor — it must pass through whatever the driver returns, including zero, without substituting a default")
                .isEqualTo(0L);
    }

    @Test
    void consumerFactoryDoesNotTouchTheResultSetOrAdvanceIt() {
        var resultSet = mock(ResultSet.class);

        new IdentityGenerator().createValueConsumer();

        verifyNoInteractions(resultSet);
    }

    @Test
    void twoConsumersFromTheSameGeneratorBothReadThroughCorrectly() throws Exception {
        var resultSet = mock(ResultSet.class);
        when(resultSet.getLong(1)).thenReturn(100L).thenReturn(200L);

        var generator = new IdentityGenerator();
        var first = generator.createValueConsumer().consume(resultSet, 1);
        var second = generator.createValueConsumer().consume(resultSet, 1);

        assertThat(first)
                .as("factory calls must be independent — reusing the same generator twice must not share state that would make the second read miss")
                .isEqualTo(100L);
        assertThat(second).isEqualTo(200L);
    }
}
