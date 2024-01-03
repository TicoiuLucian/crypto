package org.example.model.converter;

import com.opencsv.bean.AbstractBeanField;

import java.time.Instant;
import java.time.ZoneId;

public class LocalDateTimeConverter extends AbstractBeanField {

    @Override
    protected Object convert(String milliseconds) {
        return Instant.ofEpochMilli(Long.parseLong(milliseconds)).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
