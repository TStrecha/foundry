package cz.tstrecha.foundry.orm.entity.persister.generator;

import cz.tstrecha.foundry.orm.connection.DBSession;
import cz.tstrecha.foundry.orm.entity.descriptor.code.GeneratorDescriptor;
import cz.tstrecha.foundry.orm.entity.descriptor.db.SequenceDescriptor;

import java.util.function.Function;

public interface ValueGenerator<T> {

    Function<DBSession, T> createValueProvider();

    static ValueGenerator<?> fromGeneratorDescriptor(GeneratorDescriptor generatorDescriptor,
                                                     SequenceDescriptor sequenceDescriptor) {
        if(generatorDescriptor == null) {
            return null;
        }

        return switch(generatorDescriptor.strategy()) {
            case SEQUENCE -> new SequenceGenerator(sequenceDescriptor);
            case UUID -> new UUIDGenerator();
            default -> throw new UnsupportedOperationException(
                    "Generation strategy " + generatorDescriptor.strategy() + " is not implemented yet");
        };
    }


}
