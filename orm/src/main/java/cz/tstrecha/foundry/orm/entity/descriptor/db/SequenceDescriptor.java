package cz.tstrecha.foundry.orm.entity.descriptor.db;

public record SequenceDescriptor(
        String name,
        Integer incrementBy
) {

}
