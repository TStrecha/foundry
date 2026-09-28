package cz.tstrecha.foundry.entity.system;

import cz.tstrecha.foundry.definition.Column;
import cz.tstrecha.foundry.definition.Table;
import lombok.Data;

@Data
@Table("information_schema.columns")
public class ColumnsEntity {

    @Column("table_name")
    private String tableName;

    @Column("column_name")
    private String columnName;

    @Column("data_type")
    private String dataType;

    @Column("character_maximum_length")
    private String characterMaximumLength;

    @Column("is_nullable")
    private String isNullable;

    @Column("column_default")
    private String columnDefault;
}
