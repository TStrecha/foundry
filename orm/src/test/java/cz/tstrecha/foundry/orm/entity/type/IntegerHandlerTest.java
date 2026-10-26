package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegerHandlerTest {

    @Test
    void readValueDelegatesToGetInt() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getInt(3)).thenReturn(42);
        assertThat(new IntegerHandler().readValue(rs, 3)).isEqualTo(42);
    }

    @Test
    void writeValueDelegatesToSetInt() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new IntegerHandler().writeValue(stmt, 2, 99);
        verify(stmt).setInt(2, 99);
    }

    @Test
    void writeNullUsesIntegerJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new IntegerHandler().writeNull(stmt, 1);
        verify(stmt).setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getInt(1)).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new IntegerHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new IntegerHandler().write(stmt, 1, null);
        verify(stmt).setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    void writeHandlesMinAndMax() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new IntegerHandler().writeValue(stmt, 1, Integer.MIN_VALUE);
        new IntegerHandler().writeValue(stmt, 2, Integer.MAX_VALUE);
        verify(stmt).setInt(1, Integer.MIN_VALUE);
        verify(stmt).setInt(2, Integer.MAX_VALUE);
    }
}
