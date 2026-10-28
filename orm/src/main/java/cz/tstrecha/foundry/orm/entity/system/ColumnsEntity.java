package cz.tstrecha.foundry.orm.entity.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "information_schema.fields")
public class ColumnsEntity {

    @Id
    @Column(name = "table_name")
    private String tableName;

    @Column(name = "column_name")
    private String columnName;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "character_maximum_length")
    private String characterMaximumLength;

    @Column(name = "is_nullable")
    private String isNullable;

    @Column(name = "column_default")
    private String columnDefault;
}
