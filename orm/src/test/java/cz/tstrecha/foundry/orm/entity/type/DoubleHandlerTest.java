package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoubleHandlerTest {

    @Test
    void readValueDelegatesToGetDouble() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getDouble(1)).thenReturn(3.14);
        assertThat(new DoubleHandler().readValue(rs, 1)).isEqualTo(3.14);
    }

    @Test
    void writeValueDelegatesToSetDouble() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new DoubleHandler().writeValue(stmt, 2, 2.718);
        verify(stmt).setDouble(2, 2.718);
    }

    @Test
    void writeNullUsesDoubleJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new DoubleHandler().writeNull(stmt, 1);
        verify(stmt).setNull(1, JDBCType.DOUBLE.getVendorTypeNumber());
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getDouble(1)).thenReturn(0.0);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new DoubleHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new DoubleHandler().write(stmt, 1, null);
        verify(stmt).setNull(1, JDBCType.DOUBLE.getVendorTypeNumber());
    }

    @Test
    void writeHandlesInfinityAndNaN() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new DoubleHandler().writeValue(stmt, 1, Double.POSITIVE_INFINITY);
        new DoubleHandler().writeValue(stmt, 2, Double.NaN);
        verify(stmt).setDouble(1, Double.POSITIVE_INFINITY);
        verify(stmt).setDouble(2, Double.NaN);
    }
}
