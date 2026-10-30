package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalDateHandlerTest {

    @Test
    void readValueReturnsLocalDateFromSqlDate() throws Exception {
        var date = LocalDate.of(2026, 10, 30);
        var rs = mock(ResultSet.class);
        when(rs.getDate(1)).thenReturn(Date.valueOf(date));

        assertThat(new LocalDateHandler().readValue(rs, 1)).isEqualTo(date);
    }

    @Test
    void writeValueDelegatesToSetDate() throws Exception {
        var date = LocalDate.of(2026, 10, 30);
        var stmt = mock(PreparedStatement.class);
        new LocalDateHandler().writeValue(stmt, 2, date);

        verify(stmt).setDate(2, Date.valueOf(date));
    }

    @Test
    void writeNullUsesDateJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LocalDateHandler().writeNull(stmt, 1);

        verify(stmt).setNull(1, JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getDate(1)).thenReturn(null);
        when(rs.wasNull()).thenReturn(true);

        assertThat(new LocalDateHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LocalDateHandler().write(stmt, 1, null);

        verify(stmt).setNull(1, JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    void writeHandlesLeapDay() throws Exception {
        var leapDay = LocalDate.of(2024, 2, 29);
        var stmt = mock(PreparedStatement.class);
        new LocalDateHandler().writeValue(stmt, 1, leapDay);

        verify(stmt).setDate(1, Date.valueOf(leapDay));
    }

    @Test
    void writeHandlesHistoricalDate() throws Exception {
        var historicalDate = LocalDate.of(1900, 1, 1);
        var stmt = mock(PreparedStatement.class);
        new LocalDateHandler().writeValue(stmt, 1, historicalDate);

        verify(stmt).setDate(1, Date.valueOf(historicalDate));
    }

    @Test
    void writeHandlesMinSupportedDate() throws Exception {
        var date = LocalDate.of(1, 1, 1);
        var stmt = mock(PreparedStatement.class);
        new LocalDateHandler().writeValue(stmt, 1, date);

        verify(stmt).setDate(1, Date.valueOf(date));
    }
}
