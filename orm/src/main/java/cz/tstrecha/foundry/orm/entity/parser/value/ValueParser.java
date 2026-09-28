package cz.tstrecha.foundry.orm.entity.parser.value;

public interface ValueParser<T> {

    T fromString(String value);

}
