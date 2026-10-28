package cz.tstrecha.foundry.rttest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "money_transaction")
public class MoneyTransactionEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "payer")
    private String payer;

    @Column(name = "note")
    private String note;
}