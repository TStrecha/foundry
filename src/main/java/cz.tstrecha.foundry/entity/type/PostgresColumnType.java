package cz.tstrecha.foundry.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostgresColumnType implements ColumnType {
    LONG("bigint"),
    VARCHAR("character varying"),
    ;

    private final String databaseTypeName;

}
