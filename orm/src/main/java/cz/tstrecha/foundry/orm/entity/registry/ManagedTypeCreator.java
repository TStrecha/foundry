package cz.tstrecha.foundry.orm.entity.registry;

import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ManagedTypeCreator<T> {

    @Getter
    private final Class<T> type;
    private final Constructor<T> noArgsConstructor;

    public ManagedTypeCreator(Class<T> type) throws NoSuchMethodException {
        this.type = type;

        try {
            this.noArgsConstructor = type.getConstructor();
            this.noArgsConstructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            throw new NoSuchMethodException("Entities are required to have exactly one constructor with 0 arguments. No method found: " + ex.getMessage());
        }
    }

    public T createNewEmptyInstance() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return noArgsConstructor.newInstance();
    }

}
