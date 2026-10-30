package cz.tstrecha.foundry.orm.entity.descriptor.code;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

public record GeneratorDescriptor(
        GenerationType strategy,
        String name
) {

    public GeneratorDescriptor(GeneratedValue generatedValueDefinition) {
        this(generatedValueDefinition.strategy(), generatedValueDefinition.generator());
    }

}
