package cz.tstrecha.foundry.orm.entity.row;

import java.util.List;

@FunctionalInterface
public interface RowMapperBuilder<T> {

    RowMapper<T> buildMapper(List<String> columns);

}
