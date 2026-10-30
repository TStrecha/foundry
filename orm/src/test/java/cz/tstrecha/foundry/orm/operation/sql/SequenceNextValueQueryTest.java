package cz.tstrecha.foundry.orm.operation.sql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceNextValueQueryTest {

    @Test
    void buildSqlAsksPostgresForTheNextValueOfTheConfiguredSequence() {
        var query = new SequenceNextValueQuery("widget_seq");

        assertThat(query.buildSql())
                .as("the SQL must select nextval of the physical sequence — this is the one source of truth for id generation")
                .isEqualTo("SELECT nextval('widget_seq')");
    }

    @Test
    void sequenceNameIsRenderedExactlyAsGivenWithNoCaseOrQuotingTransformation() {
        var query = new SequenceNextValueQuery("Mixed_Case_Seq");

        assertThat(query.buildSql())
                .as("the name comes from @SequenceGenerator — must be passed to PostgreSQL untouched so case-sensitive identifiers still address the right sequence")
                .contains("'Mixed_Case_Seq'");
    }
}
