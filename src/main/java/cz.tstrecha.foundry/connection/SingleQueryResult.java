package cz.tstrecha.foundry.connection;

import java.util.LinkedList;
import java.util.Set;

public record SingleQueryResult(Set<String> columnLabels, LinkedList<String> row) {

    private int columnCount() {
        return columnLabels.size();
    }
}
