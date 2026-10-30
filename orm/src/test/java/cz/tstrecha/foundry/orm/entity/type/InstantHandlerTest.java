package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InstantHandlerTest {

    @Test
    void readValueReturnsInstantFromTimestamp() throws Exception {
        var moment = Instant.parse("2026-10-30T14:35:22.123Z");
        var rs = mock(ResultSet.class);
        when(rs.getTimestamp(1)).thenReturn(Timestamp.from(moment));

        assertThat(new InstantHandler().readValue(rs, 1)).isEqualTo(moment);
    }

    @Test
    void writeValuePreservesTimeOfDay() throws Exception {
        var moment = Instant.parse("2026-10-30T14:35:22.123Z");
        var stmt = mock(PreparedStatement.class);
        new InstantHandler().writeValue(stmt, 2, moment);

        verify(stmt).setTimestamp(2, Timestamp.from(moment));
    }

    @Test
    void writeNullUsesTimestampJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new InstantHandler().writeNull(stmt, 1);

        verify(stmt).setNull(eq(1), intThat(code ->
                code == JDBCType.TIMESTAMP.getVendorTypeNumber()
             || code == JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber()));
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getTimestamp(1)).thenReturn(null);
        when(rs.wasNull()).thenReturn(true);

        assertThat(new InstantHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new InstantHandler().write(stmt, 1, null);

        verify(stmt).setNull(eq(1), intThat(code ->
                code == JDBCType.TIMESTAMP.getVendorTypeNumber()
             || code == JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber()));
    }

    @Test
    void writeHandlesEpoch() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new InstantHandler().writeValue(stmt, 1, Instant.EPOCH);

        verify(stmt).setTimestamp(1, Timestamp.from(Instant.EPOCH));
    }

    @Test
    void writeHandlesNanosecondPrecision() throws Exception {
        var moment = Instant.ofEpochSecond(1_700_000_000L, 123_456_789);
        var stmt = mock(PreparedStatement.class);
        new InstantHandler().writeValue(stmt, 1, moment);

        verify(stmt).setTimestamp(1, Timestamp.from(moment));
    }

    @Test
    void writeHandlesInstantFarInThePast() throws Exception {
        var moment = Instant.parse("1900-01-01T00:00:00Z");
        var stmt = mock(PreparedStatement.class);
        new InstantHandler().writeValue(stmt, 1, moment);

        verify(stmt).setTimestamp(1, Timestamp.from(moment));
    }
}
