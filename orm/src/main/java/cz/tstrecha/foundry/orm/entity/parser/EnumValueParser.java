package cz.tstrecha.foundry.orm.entity.parser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnumValueParser<T extends Enum<T>> implements ValueParser<Enum<T>> {

    private final Class<T> type;

    @Override
    public Enum<T> fromString(String value) {
        return Enum.valueOf(type, value);
    }

}
