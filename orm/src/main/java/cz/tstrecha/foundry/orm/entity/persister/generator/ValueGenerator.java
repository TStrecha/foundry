package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.entity.descriptor.code.GeneratorDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;

public interface ValueGenerator<T> {

    static ValueGenerator<?> fromGeneratorDescriptor(GeneratorDescriptor generatorDescriptor,
                                                     SequenceDescriptor sequenceDescriptor) {
        if (generatorDescriptor == null) {
            return null;
        }

        return switch (generatorDescriptor.strategy()) {
            case SEQUENCE -> new SequenceGenerator(sequenceDescriptor);
            case UUID -> new UUIDGenerator();
            case IDENTITY -> new IdentityGenerator();
            case AUTO -> throw new IllegalStateException("Specify a concrete strategy, AUTO is not supported");
            case TABLE -> throw new UnsupportedOperationException(
                    "Generation strategy TABLE is not implemented yet");
        };
    }

}
