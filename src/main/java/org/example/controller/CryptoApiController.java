package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.example.model.Crypto;
import org.example.model.Symbol;
import org.example.service.CryptoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/crypto")
public class CryptoApiController implements CryptoApi {

    private final CryptoService cryptoService;

    public ResponseEntity<List<Crypto>> getSortedCryptosByNormalizedRange() {
        return new ResponseEntity<>(cryptoService.calculateNormalizedRangesAndSort(), HttpStatus.OK);
    }

    public ResponseEntity<Map<String, List<Crypto>>> getStatisticsForMonthAndYear(@PathVariable int year, @PathVariable int month) {
        return new ResponseEntity<>(cryptoService.getStatisticsByMonthAndYear(year, month), HttpStatus.OK);
    }

    public ResponseEntity<List<Pair<String, Crypto>>> getStatisticsForCrypto(@PathVariable Symbol crypto) {
        return new ResponseEntity<>(cryptoService.getStatisticsForCertainCrypto(crypto), HttpStatus.OK);
    }

    public ResponseEntity<Crypto> getHighestNormalizedRange(@RequestBody LocalDate date) {
        return new ResponseEntity<>(cryptoService.getCryptoWithHighestNormalizedRange(date), HttpStatus.OK);
    }
}
