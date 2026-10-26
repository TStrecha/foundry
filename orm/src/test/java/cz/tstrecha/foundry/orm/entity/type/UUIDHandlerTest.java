package cz.tstrecha.foundry.orm.entity.type;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UUIDHandlerTest {

    private final UUID sample = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void readValueParsesUuidFromString() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn(sample.toString());
        assertThat(new UUIDHandler().readValue(rs, 1)).isEqualTo(sample);
    }

    @Test
    void writeValueWritesUuidString() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new UUIDHandler().writeValue(stmt, 2, sample);
        verify(stmt).setString(2, sample.toString());
    }

    @Test
    void readMapsSqlNullToJavaNull() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn(null);
        when(rs.wasNull()).thenReturn(true);
        assertThat(new UUIDHandler().read(rs, 1)).isNull();
    }

    @Test
    void writeWithNullValueDoesNotThrow() throws Exception {
        var stmt = mock(PreparedStatement.class);
        new UUIDHandler().write(stmt, 1, null);
        verify(stmt, never()).setString(1, null);
    }
}
