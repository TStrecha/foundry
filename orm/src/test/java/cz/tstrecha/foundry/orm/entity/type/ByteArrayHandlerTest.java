package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ByteArrayHandlerTest {

    @Test
    void readValueDelegatesToGetBytes() throws Exception {
        var rs = mock(ResultSet.class);
        var bytes = new byte[]{1, 2, 3};
        when(rs.getBytes(1)).thenReturn(bytes);
        assertThat(new ByteArrayHandler().readValue(rs, 1)).containsExactly(1, 2, 3);
    }

    @Test
    void writeValueDelegatesToSetBytes() throws Exception {
        var stmt = mock(PreparedStatement.class);
        var bytes = new byte[]{10, 20, 30};
        new ByteArrayHandler().writeValue(stmt, 2, bytes);
        verify(stmt).setBytes(2, bytes);
    }

    @Test
    void writeNullUsesBinaryJdbcType() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new ByteArrayHandler().writeNull(stmt, 1);
        // byte[] maps to SQL BINARY/VARBINARY/BLOB — VARCHAR is a textual type and
        // strict drivers will refuse it on a binary column.
        verify(stmt).setNull(eq(1), intThat(code ->
                code == JDBCType.VARBINARY.getVendorTypeNumber()
             || code == JDBCType.BINARY.getVendorTypeNumber()
             || code == JDBCType.LONGVARBINARY.getVendorTypeNumber()
             || code == JDBCType.BLOB.getVendorTypeNumber()));
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getBytes(1)).thenReturn(null);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new ByteArrayHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDelegatesToWriteNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new ByteArrayHandler().write(stmt, 1, null);
        verify(stmt, never()).setBytes(1, null);
    }

    @Test
    void writeHandlesEmptyArray() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new ByteArrayHandler().writeValue(stmt, 1, new byte[0]);
        verify(stmt).setBytes(eq(1), argThat(arr -> arr.length == 0));
    }
}
