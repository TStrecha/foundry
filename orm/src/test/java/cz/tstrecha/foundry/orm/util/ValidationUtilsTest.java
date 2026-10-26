package cz.tstrecha.foundry.orm.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotEmptyArgument;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotEmptyState;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullState;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireValidSqlIdentifier;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilsTest {

    // -------- requireNotNullArgument --------

    @Test
    void requireNotNullArgument_rejectsNullWithIllegalArgumentException() {
        assertThatThrownBy(() -> requireNotNullArgument(null, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field");
    }

    @Test
    void requireNotNullArgument_passesForNonNull() {
        assertThatCode(() -> requireNotNullArgument("x", "field")).doesNotThrowAnyException();
        assertThatCode(() -> requireNotNullArgument(42, "field")).doesNotThrowAnyException();
        assertThatCode(() -> requireNotNullArgument(List.of(), "field")).doesNotThrowAnyException();
    }

    // -------- requireNotNullState --------

    @Test
    void requireNotNullState_rejectsNullWithIllegalStateException() {
        assertThatThrownBy(() -> requireNotNullState(null, "field"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("field");
    }

    @Test
    void requireNotNullState_passesForNonNull() {
        assertThatCode(() -> requireNotNullState("x", "field")).doesNotThrowAnyException();
    }

    // -------- requireNotEmptyArgument --------

    @Test
    void requireNotEmptyArgument_rejectsNull() {
        assertThatThrownBy(() -> requireNotEmptyArgument(null, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field");
    }

    @Test
    void requireNotEmptyArgument_rejectsEmptyCollection() {
        assertThatThrownBy(() -> requireNotEmptyArgument(List.of(), "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field");
    }

    @Test
    void requireNotEmptyArgument_rejectsEmptyMap() {
        assertThatThrownBy(() -> requireNotEmptyArgument(Map.of(), "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field");
    }

    @Test
    void requireNotEmptyArgument_rejectsEmptyString() {
        assertThatThrownBy(() -> requireNotEmptyArgument("", "field"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireNotEmptyArgument_rejectsBlankString() {
        assertThatThrownBy(() -> requireNotEmptyArgument("   \t\n", "field"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireNotEmptyArgument_passesForNonEmptyCollection() {
        assertThatCode(() -> requireNotEmptyArgument(List.of("a"), "field")).doesNotThrowAnyException();
    }

    @Test
    void requireNotEmptyArgument_passesForNonBlankString() {
        assertThatCode(() -> requireNotEmptyArgument("hello", "field")).doesNotThrowAnyException();
    }

    @Test
    void requireNotEmptyArgument_passesForOtherNonNullTypes() {
        assertThatCode(() -> requireNotEmptyArgument(42, "field")).doesNotThrowAnyException();
        assertThatCode(() -> requireNotEmptyArgument(new Object(), "field")).doesNotThrowAnyException();
    }

    // -------- requireNotEmptyState --------

    @Test
    void requireNotEmptyState_rejectsNullWithIllegalStateException() {
        assertThatThrownBy(() -> requireNotEmptyState(null, "field"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requireNotEmptyState_rejectsEmptyCollectionWithIllegalStateException() {
        assertThatThrownBy(() -> requireNotEmptyState(List.of(), "field"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requireNotEmptyState_rejectsBlankStringWithIllegalStateException() {
        assertThatThrownBy(() -> requireNotEmptyState(" ", "field"))
                .isInstanceOf(IllegalStateException.class);
    }

    // -------- requireValidSqlIdentifier --------

    @Test
    void requireValidSqlIdentifier_passesForLettersOnly() {
        assertThatCode(() -> requireValidSqlIdentifier("name", "col")).doesNotThrowAnyException();
    }

    @Test
    void requireValidSqlIdentifier_passesForLettersAndDigits() {
        assertThatCode(() -> requireValidSqlIdentifier("col1", "col")).doesNotThrowAnyException();
    }

    @Test
    void requireValidSqlIdentifier_passesForUnderscores() {
        assertThatCode(() -> requireValidSqlIdentifier("my_column_1", "col")).doesNotThrowAnyException();
    }

    @Test
    void requireValidSqlIdentifier_passesForLeadingUnderscore() {
        assertThatCode(() -> requireValidSqlIdentifier("_hidden", "col")).doesNotThrowAnyException();
    }

    @Test
    void requireValidSqlIdentifier_rejectsNull() {
        assertThatThrownBy(() -> requireValidSqlIdentifier(null, "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsEmpty() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsBlank() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("   ", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsLeadingDigit() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("1column", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsSpace() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("my column", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsHyphen() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("my-column", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_acceptsSchemaQualifiedTableName() {
        assertThatCode(() -> requireValidSqlIdentifier("information_schema.columns", "table"))
                .doesNotThrowAnyException();
    }

    @Test
    void requireValidSqlIdentifier_acceptsCatalogSchemaTableChain() {
        assertThatCode(() -> requireValidSqlIdentifier("catalog.schema.table", "table"))
                .doesNotThrowAnyException();
    }

    @Test
    void requireValidSqlIdentifier_rejectsLeadingDot() {
        assertThatThrownBy(() -> requireValidSqlIdentifier(".table", "table"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsTrailingDot() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("table.", "table"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsConsecutiveDots() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("schema..table", "table"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsSegmentStartingWithDigit() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("schema.1table", "table"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsDoubleQuote() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("col\"", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsSemicolon() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("col;DROP", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsStarWildcard() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("*", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_rejectsUnicodeLettersBeyondAscii() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("čeština", "col"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireValidSqlIdentifier_errorMessageContainsFieldNameAndValue() {
        assertThatThrownBy(() -> requireValidSqlIdentifier("1bad", "columnLabel"))
                .hasMessageContaining("columnLabel")
                .hasMessageContaining("1bad");
    }
}
