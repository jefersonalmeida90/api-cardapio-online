package br.com.cardapioonline.application.common;

import java.util.Arrays;

public final class EnumMapper {

    private EnumMapper() {
    }

    public static <E extends Enum<E>> E parseIgnoreCase(Class<E> enumClass, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(fallback);
    }
}
