package cz.tstrecha.foundry.orm.connection;

import java.util.LinkedList;
import java.util.Set;

public record QueryResult(Set<String> columnLabels, LinkedList<LinkedList<String>> rows) {

    private int columnCount() {
        return columnLabels.size();
    }

    private int rowCount() {
        return rows.size();
    }
}
