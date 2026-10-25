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

class EnumHandlerTest {

    @Test
    void writeStoresEnumNameAsString() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new EnumHandler<>(Genre.class).write(stmt, 1, Genre.FICTION);
        verify(stmt).setString(1, "FICTION");
    }

    @Test
    void readParsesEnumByName() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("POETRY");
        assertThat(new EnumHandler<>(Genre.class).read(rs, 1)).isEqualTo(Genre.POETRY);
    }

    @Test
    void readThrowsForUnknownEnumValue() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("WUT");
        assertThatThrownBy(() -> new EnumHandler<>(Genre.class).read(rs, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn(null);
        assertThat(new EnumHandler<>(Genre.class).read(rs, 1)).isNull();
    }

    @Test
    void writeMapsJavaNullToSqlNull() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new EnumHandler<>(Genre.class).write(stmt, 1, null);
        verify(stmt).setNull(1, Types.VARCHAR);
    }

    @Test
    void readIsCaseSensitive() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("fiction");
        assertThatThrownBy(() -> new EnumHandler<>(Genre.class).read(rs, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullEnumClassIsRejected() {
        assertThatThrownBy(() -> new EnumHandler<>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
