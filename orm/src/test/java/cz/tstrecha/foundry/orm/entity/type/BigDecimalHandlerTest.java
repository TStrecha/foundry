package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BigDecimalHandlerTest {

    @Test
    void readValueDelegatesToGetBigDecimal() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getBigDecimal(1)).thenReturn(new BigDecimal("12.345"));
        assertThat(new BigDecimalHandler().readValue(rs, 1)).isEqualByComparingTo("12.345");
    }

    @Test
    void writeValueDelegatesToSetBigDecimal() throws Exception {
        var stmt = mock(PreparedStatement.class);
        var value = new BigDecimal("999.999");
        new BigDecimalHandler().writeValue(stmt, 2, value);
        verify(stmt).setBigDecimal(2, value);
    }

    @Test
    void writeNullUsesNumericOrDecimalJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new BigDecimalHandler().writeNull(stmt, 1);
        // BigDecimal maps to SQL NUMERIC/DECIMAL. DOUBLE is a different type family
        // (floating-point) and can be refused by strict drivers on NUMERIC columns.
        verify(stmt).setNull(eq(1), intThat(code ->
                code == JDBCType.NUMERIC.getVendorTypeNumber()
             || code == JDBCType.DECIMAL.getVendorTypeNumber()));
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getBigDecimal(1)).thenReturn(null);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new BigDecimalHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new BigDecimalHandler().write(stmt, 1, null);
        verify(stmt).setNull(eq(1), intThat(code ->
                code == JDBCType.NUMERIC.getVendorTypeNumber()
             || code == JDBCType.DECIMAL.getVendorTypeNumber()));
    }

    @Test
    void writeHandlesLargeValueBeyondDoubleRange() throws Exception {
        var stmt = mock(PreparedStatement.class);
        var huge = new BigDecimal("12345678901234567890.1234567890123456789");
        new BigDecimalHandler().writeValue(stmt, 1, huge);
        verify(stmt).setBigDecimal(1, huge);
    }
}
