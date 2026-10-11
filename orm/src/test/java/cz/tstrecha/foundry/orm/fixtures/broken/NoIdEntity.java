package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity without any @Id annotation.
 */
@Data
@NoArgsConstructor
@Table("no_id")
public class NoIdEntity {

    @Column("name")
    private String name;
}
