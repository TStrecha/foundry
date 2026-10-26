package cz.tstrecha.foundry.orm.entity.system;

import cz.tstrecha.foundry.orm.definition.Column;
import lombok.Data;

@Data
public class ColumnsEntityId {

    @Column("table_catalog")
    private String tableCatalog;

    @Column("table_schema")
    private String tableSchema;

    @Column("table_name")
    private String tableName;

    @Column("column_name")
    private String columnName;

}
