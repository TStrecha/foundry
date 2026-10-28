package cz.tstrecha.foundry.orm.connection.sql;

import cz.tstrecha.foundry.orm.operation.filter.SqlOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SqlOperatorTest {

    @ParameterizedTest
    @CsvSource({
            "EQUALS,=",
            "GREATER,>",
            "GREATER_OR_EQUAL,>=",
            "LESSER,<",
            "LESSER_OR_EQUAL,<=",
            "LIKE,LIKE"
    })
    void returnsExpectedOperatorToken(SqlOperator op, String token) {
        assertThat(op.getSqlOperator()).isEqualTo(token);
    }

    @Test
    void enumCoversExactlyTheKnownOperators() {
        assertThat(SqlOperator.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder(
                        "EQUALS", "GREATER", "GREATER_OR_EQUAL",
                        "LESSER", "LESSER_OR_EQUAL", "LIKE");
    }
}
