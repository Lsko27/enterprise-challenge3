package br.com.fiap.enterprise_challenge3.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToNumberConverter
        implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer valorBanco) {
        return valorBanco != null && valorBanco == 1;
    }
}