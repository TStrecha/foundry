package cz.tstrecha.foundry.orm.entity.parser.value;

public class StringValueParser implements ValueParser<String> {

    @Override
    public String fromString(String value) {
        return value;
    }

}
