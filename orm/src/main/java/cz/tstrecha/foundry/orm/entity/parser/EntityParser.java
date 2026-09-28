package cz.tstrecha.foundry.orm.entity.parser;

import cz.tstrecha.foundry.orm.entity.EntityContext;
import cz.tstrecha.foundry.orm.entity.EntityDefinition;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

@RequiredArgsConstructor
public class EntityParser {

    private final EntityContext entityContext;

    @SneakyThrows
    public <T> T parse(Class<T> clazz, List<String> columnLabels, List<String> row, EntityDefinition<T> definitions) {
        var entityCreationStrategy = entityContext.getEntityCreationStrategy(clazz);
        var entity = entityCreationStrategy.noArgConstructor().newInstance();

        for(int i = 0; i < columnLabels.size(); i++) {
            var label = columnLabels.get(i);
            var value = row.get(i);

            var columnDefinition = definitions.columns().get(label);
            var parser = entityCreationStrategy.parameterStrategies().get(columnDefinition.field()).parser();
            parser.parse(entity, value);
        }

        return entity;
    }
}
