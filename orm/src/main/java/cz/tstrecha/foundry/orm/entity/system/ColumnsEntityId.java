package cz.tstrecha.foundry.orm.entity.system;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ColumnsEntityId {

    @Id
    @Column(name = "table_catalog")
    private String tableCatalog;

    @Column(name = "table_schema")
    private String tableSchema;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "column_name")
    private String columnName;

}
