package cz.tstrecha.foundry.orm.entity.descriptor.db;

import jakarta.persistence.SequenceGenerator;

public record SequenceDescriptor(
        String name,
        String sequenceName
) {

    public SequenceDescriptor(SequenceGenerator sequenceGenerator) {
        this(sequenceGenerator.name(), sequenceGenerator.sequenceName());
    }

}
