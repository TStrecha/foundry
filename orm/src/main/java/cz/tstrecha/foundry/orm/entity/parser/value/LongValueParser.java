package cz.tstrecha.foundry.orm.entity.parser.value;

public class LongValueParser implements ValueParser<Long> {

    @Override
    public Long fromString(String value) {
        return Long.valueOf(value);
    }
    
}
