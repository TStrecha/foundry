package cz.tstrecha.foundry.orm.entity.system;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.EmbeddedId;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;

@Data
@Table("information_schema.columns")
public class ColumnsEntity {

    @EmbeddedId
    private ColumnsEntityId id;

    @Column("data_type")
    private String dataType;

    @Column("character_maximum_length")
    private String characterMaximumLength;

    @Column("is_nullable")
    private String isNullable;

    @Column("column_default")
    private String columnDefault;
}
