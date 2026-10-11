package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table("multi_id")
public class MultipleIdsEntity {

    @Id
    @Column("id1")
    private Long id1;

    @Id
    @Column("id2")
    private Long id2;
}
