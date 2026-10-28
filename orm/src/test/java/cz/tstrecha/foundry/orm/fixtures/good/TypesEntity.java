package cz.tstrecha.foundry.orm.fixtures.good;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity(name = "types_entity")
public class TypesEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid_col")
    private UUID uuidCol;

    @Column(name = "decimal_col")
    private BigDecimal decimalCol;

    @Column(name = "integer_col")
    private Integer integerCol;

    @Column(name = "double_col")
    private Double doubleCol;

    @Column(name = "boolean_col")
    private Boolean booleanCol;

    @Column(name = "bytes_col")
    private byte[] bytesCol;
}
