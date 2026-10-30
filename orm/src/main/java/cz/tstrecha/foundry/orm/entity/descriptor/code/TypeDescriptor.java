package cz.tstrecha.foundry.orm.entity.descriptor.code;

import java.util.List;

public interface TypeDescriptor {

    Class<?> type();
    List<FieldDescriptor> fields();

}
