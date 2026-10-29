package cz.tstrecha.foundry.orm.entity.system;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity(name = "information_schema.columns")
public class ColumnsEntity {

    @EmbeddedId
    private ColumnsEntityId id;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "character_maximum_length")
    private String characterMaximumLength;

    @Column(name = "is_nullable")
    private String isNullable;

    @Column(name = "column_default")
    private String columnDefault;
}
