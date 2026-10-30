package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;
import cz.tstrecha.foundry.orm.operation.sql.SequenceNextValueQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SequenceGeneratorTest {

    private static final SequenceDescriptor WIDGET_SEQ =
            new SequenceDescriptor("widget_seq_gen", "widget_seq");

    @Test
    void providerAsksSessionForNextValueOfTheConfiguredSequence() {
        var session = mock(DBSession.class);
        var generator = new SequenceGenerator(WIDGET_SEQ);

        generator.createValueProvider().apply(session);

        var captor = ArgumentCaptor.forClass(SequenceNextValueQuery.class);
        verify(session).selectNextValueFromSequence(captor.capture());
        assertThat(captor.getValue().buildSql())
                .as("the provider must call the physical sequence — not the generator alias, otherwise Postgres would complain the identifier does not exist")
                .contains("widget_seq")
                .doesNotContain("widget_seq_gen");
    }

    @Test
    void providerReturnsWhateverTheSessionReturns() {
        var session = mock(DBSession.class);
        when(session.selectNextValueFromSequence(any())).thenReturn(42L);

        var value = new SequenceGenerator(WIDGET_SEQ).createValueProvider().apply(session);

        assertThat(value)
                .as("the generator is a thin dispatcher — it must not transform the value coming back from the session")
                .isEqualTo(42L);
    }

    @Test
    void nullSequenceDescriptorInConstructorIsRejected() {
        assertThatThrownBy(() -> new SequenceGenerator(null))
                .as("constructing a sequence generator without a descriptor can only be a caller bug — must be rejected explicitly, not surface as an NPE on first use")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twoProvidersFromTheSameGeneratorEachIssueTheirOwnSessionCall() {
        var session = mock(DBSession.class);
        var generator = new SequenceGenerator(WIDGET_SEQ);

        generator.createValueProvider().apply(session);
        generator.createValueProvider().apply(session);

        verify(session, org.mockito.Mockito.times(2)).selectNextValueFromSequence(any());
    }
}
