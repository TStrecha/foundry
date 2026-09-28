package cz.tstrecha.foundry.orm.entity.scan;

import cz.tstrecha.foundry.orm.entity.EntityCreationStrategy;
import cz.tstrecha.foundry.orm.entity.EntityDefinition;

import java.util.Map;
import java.util.Set;

public record EntityScanResult(Set<Class<?>> scannedEntities,
                               Map<Class<?>, EntityDefinition<?>> entityDefinitions,
                               Map<Class<?>, EntityCreationStrategy<?>> entityCreationStrategies) {

}
