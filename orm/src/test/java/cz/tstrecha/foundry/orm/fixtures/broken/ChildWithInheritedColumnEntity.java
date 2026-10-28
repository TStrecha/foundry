package cz.tstrecha.foundry.orm.fixtures.broken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Entity(name = "child_entity")
public class ChildWithInheritedColumnEntity extends ParentWithColumn {

    @Column(name = "name")
    private String name;
}

@Data
@NoArgsConstructor
abstract class ParentWithColumn {

    @Id
    @Column(name = "id")
    private Long id;
}
