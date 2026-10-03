package cz.tstrecha.foundry.orm.entity.parser;

public class StringValueParser implements ValueParser<String> {

    @Override
    public String fromString(String value) {
        return value;
    }

}
