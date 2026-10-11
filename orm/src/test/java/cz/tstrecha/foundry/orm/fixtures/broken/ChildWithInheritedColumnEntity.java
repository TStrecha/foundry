package cz.tstrecha.foundry.orm.fixtures.broken;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Child inherits an annotated field from an abstract parent. The ORM uses
 * {@code getDeclaredFields()} which does NOT include inherited fields — so the
 * inherited id would be invisible to ManagedType.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Table("child_entity")
public class ChildWithInheritedColumnEntity extends ParentWithColumn {

    @Column("name")
    private String name;
}

@Data
@NoArgsConstructor
abstract class ParentWithColumn {

    @Id
    @Column("id")
    private Long id;
}
