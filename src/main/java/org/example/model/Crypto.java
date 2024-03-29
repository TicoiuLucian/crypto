package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.*;
import org.example.model.converter.LocalDateTimeConverter;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Crypto {

  @CsvCustomBindByPosition(position = 0, converter = LocalDateTimeConverter.class)
  private LocalDateTime timestamp;

  @CsvBindByPosition(position = 1)
  private Symbol symbol;

  @CsvBindByPosition(position = 2)
  private Double price;

  @JsonIgnore
  private Double normalizedRange;
}
