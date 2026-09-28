package cz.tstrecha.foundry.rttest.entity;

import cz.tstrecha.foundry.orm.definition.Column;
import cz.tstrecha.foundry.orm.definition.Id;
import cz.tstrecha.foundry.orm.definition.Table;
import lombok.Data;

@Data
@Table("app_user")
public class UserEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("company_name")
    private String companyName;

    @Column("account_type")
    private AccountType accountType;
}
