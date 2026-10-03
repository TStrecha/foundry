package cz.tstrecha.foundry.orm.entity.parser;

public interface ValueParser<T> {

    T fromString(String value);

}
