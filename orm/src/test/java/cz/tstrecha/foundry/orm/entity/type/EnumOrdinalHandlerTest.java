package cz.tstrecha.foundry.orm.entity.type;

import cz.tstrecha.foundry.orm.fixtures.Genre;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnumOrdinalHandlerTest {

    @Test
    void writeStoresEnumOrdinalAsInt() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new EnumOrdinalHandler<>(Genre.class).write(stmt, 1, Genre.POETRY);
        verify(stmt).setInt(1, Genre.POETRY.ordinal());
    }

    @Test
    void readResolvesEnumByOrdinal() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getInt(1)).thenReturn(Genre.NON_FICTION.ordinal());
        assertThat(new EnumOrdinalHandler<>(Genre.class).read(rs, 1)).isEqualTo(Genre.NON_FICTION);
    }

    @Test
    void readThrowsForOrdinalOutOfRange() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getInt(1)).thenReturn(99);
        assertThatThrownBy(() -> new EnumOrdinalHandler<>(Genre.class).read(rs, 1))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getInt(1)).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new EnumOrdinalHandler<>(Genre.class).read(rs, 1)).isNull();
    }

    @Test
    void writeMapsJavaNullToSqlNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new EnumOrdinalHandler<>(Genre.class).write(stmt, 1, null);
        verify(stmt).setNull(1, Types.INTEGER);
    }

    @Test
    void nullEnumClassIsRejected() {
        assertThatThrownBy(() -> new EnumOrdinalHandler<>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
