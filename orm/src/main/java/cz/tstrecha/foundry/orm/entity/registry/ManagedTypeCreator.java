package cz.tstrecha.foundry.orm.entity.registry;

import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

public class ManagedTypeCreator<T> {

    @Getter
    private final Class<T> type;
    private final Constructor<T> noArgsConstructor;

    public ManagedTypeCreator(Class<T> type) throws NoSuchMethodException {
        requireNotNullArgument(type, "type");

        this.type = type;
        if(Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException(type.getName() + " is abstract");
        }

        try {
            this.noArgsConstructor = type.getDeclaredConstructor();
            this.noArgsConstructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            throw new NoSuchMethodException("Entities are required to have exactly one constructor with 0 arguments. No method found: " + ex.getMessage());
        }
    }

    public T createNewEmptyInstance() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return noArgsConstructor.newInstance();
    }

}
