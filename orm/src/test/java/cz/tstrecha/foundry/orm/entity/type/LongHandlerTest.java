package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LongHandlerTest {

    @Test
    void writeDelegatesToSetLong() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LongHandler().write(stmt, 3, 42L);
        verify(stmt).setLong(3, 42L);
    }

    @Test
    void readDelegatesToGetLong() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getLong(5)).thenReturn(99L);
        assertThat(new LongHandler().read(rs, 5)).isEqualTo(99L);
    }

    @Test
    void writeMapsJavaNullToSqlNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LongHandler().write(stmt, 1, null);
        verify(stmt).setNull(1, Types.BIGINT);
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getLong(1)).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new LongHandler().read(rs, 1)).isNull();
    }

    @Test
    void readReturnsExplicitZeroWhenWasNullIsFalse() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getLong(1)).thenReturn(0L);
        when(rs.wasNull()).thenReturn(false);
        assertThat(new LongHandler().read(rs, 1)).isEqualTo(0L);
    }

    @Test
    void writeHandlesMinValue() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LongHandler().write(stmt, 1, Long.MIN_VALUE);
        verify(stmt).setLong(1, Long.MIN_VALUE);
    }

    @Test
    void writeHandlesMaxValue() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LongHandler().write(stmt, 1, Long.MAX_VALUE);
        verify(stmt).setLong(1, Long.MAX_VALUE);
    }

    @Test
    void writeHandlesNegativeValue() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new LongHandler().write(stmt, 1, -42L);
        verify(stmt).setLong(1, -42L);
    }
}
