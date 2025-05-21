package tqs.electro.electro.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChargerType {

    TYPE1("Type 1"),
    TYPE2("Type 2"),
    CCS("CCS"),
    CHADEMO("CHAdeMO"),
    TESLA("Tesla"),
    SCHUKO("Schuko");

    private final String value;

    ChargerType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ChargerType fromValue(String value) {
        for (ChargerType type : ChargerType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown plug type: " + value);
    }

}
