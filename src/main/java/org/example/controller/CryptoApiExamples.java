package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Crypto;
import org.example.model.Symbol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CryptoApiExamples {
    final static List<Crypto> CRYPTOS = List.of(new Crypto(LocalDateTime.of(2023, 10, 5, 12, 12), Symbol.BTC, 2615.75D, null));

    public static String getJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(CRYPTOS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
