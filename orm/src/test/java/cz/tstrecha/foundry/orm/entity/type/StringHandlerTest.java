package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StringHandlerTest {

    @Test
    void writeDelegatesToSetString() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new StringHandler().write(stmt, 2, "hello");
        verify(stmt).setString(2, "hello");
    }

    @Test
    void writeAllowsNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new StringHandler().write(stmt, 2, null);
        verify(stmt).setNull(2, JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    void readDelegatesToGetString() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("foo");
        assertThat(new StringHandler().read(rs, 1)).isEqualTo("foo");
    }

    @Test
    void readPropagatesNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn(null);
        assertThat(new StringHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeEmptyStringIsDistinctFromNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new StringHandler().write(stmt, 1, "");
        verify(stmt).setString(1, "");
    }

    @Test
    void writeHandlesUnicode() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new StringHandler().write(stmt, 1, "ěščřžýáíéňťď 中文 😀");
        verify(stmt).setString(1, "ěščřžýáíéňťď 中文 😀");
    }

    @Test
    void writeHandlesStringWithSingleQuote() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new StringHandler().write(stmt, 1, "O'Brian");
        verify(stmt).setString(1, "O'Brian");
    }
}
