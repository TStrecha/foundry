package cz.tstrecha.foundry.orm.fixtures.good;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("types_entity")
public class TypesEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("uuid_col")
    private UUID uuidCol;

    @Column("decimal_col")
    private BigDecimal decimalCol;

    @Column("integer_col")
    private Integer integerCol;

    @Column("double_col")
    private Double doubleCol;

    @Column("boolean_col")
    private Boolean booleanCol;

    @Column("bytes_col")
    private byte[] bytesCol;
}
