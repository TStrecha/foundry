package cz.tstrecha.foundry.entity;

import cz.tstrecha.foundry.connection.FoundryContext;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityParser {

    @SneakyThrows
    public static <T> T parse(Class<T> clazz, Set<String> columnLabels, List<String> row, EntityDefinition definitions) {

        var labelsList = new ArrayList<>(columnLabels);
        var entityCreationStrategy = (EntityCreationStrategy<T>) FoundryContext.getEntityCreationStrategies().get(clazz);
        var entity = entityCreationStrategy.noArgConstructor().newInstance();

        for(int i = 0; i < columnLabels.size(); i++) {
            var label = labelsList.get(i);
            var value = row.get(i);

            var columnDefinition = definitions.columns().get(label);
            var setter = FoundryContext.getEntityCreationStrategies().get(clazz).parameterStrategies().get(columnDefinition.field()).setter();
            setter.invoke(entity, value);
        }

        return entity;
    }
}
