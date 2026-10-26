package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BooleanHandlerTest {

    @Test
    void readValueDelegatesToGetBoolean() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getBoolean(1)).thenReturn(true);
        assertThat(new BooleanHandler().readValue(rs, 1)).isTrue();
    }

    @Test
    void writeValueDelegatesToSetBoolean() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new BooleanHandler().writeValue(stmt, 2, true);
        verify(stmt).setBoolean(2, true);
    }

    @Test
    void writeNullUsesBooleanJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new BooleanHandler().writeNull(stmt, 1);
        verify(stmt).setNull(1, JDBCType.BOOLEAN.getVendorTypeNumber());
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getBoolean(1)).thenReturn(false);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new BooleanHandler().read(rs, 1)).isNull();
    }

    @Test
    void readPreservesFalseWhenNotNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getBoolean(1)).thenReturn(false);
        when(rs.wasNull()).thenReturn(false);
        assertThat(new BooleanHandler().read(rs, 1)).isFalse();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new BooleanHandler().write(stmt, 1, null);
        verify(stmt).setNull(1, JDBCType.BOOLEAN.getVendorTypeNumber());
    }
}
