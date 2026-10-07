package cz.tstrecha.foundry.rttest.entity;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("money_transaction")
public class MoneyTransactionEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("type")
    private TransactionType type;

    @Column("identifier")
    private String identifier;

    @Column("amount")
    private Long amount;

    @Column("category_id")
    private Long categoryId;

    @Column("payer")
    private String payer;

    @Column("note")
    private String note;
}