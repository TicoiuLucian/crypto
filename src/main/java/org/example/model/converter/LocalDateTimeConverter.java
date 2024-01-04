package org.example.model.converter;

import com.opencsv.bean.AbstractBeanField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeConverter extends AbstractBeanField<String, LocalDateTime> {

  @Override
  protected Object convert(String milliseconds) {
    return Instant.ofEpochMilli(Long.parseLong(milliseconds)).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }
}
