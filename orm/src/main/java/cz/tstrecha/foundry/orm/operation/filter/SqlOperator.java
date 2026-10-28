package cz.tstrecha.foundry.orm.operation.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SqlOperator {
    EQUALS("="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESSER("<"),
    LESSER_OR_EQUAL("<="),
    LIKE("LIKE");

    @Getter
    private final String sqlOperator;
}
