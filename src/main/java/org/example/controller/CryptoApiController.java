package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.example.annotation.RateLimited;
import org.example.exception.InvalidDateException;
import org.example.model.Crypto;
import org.example.model.Symbol;
import org.example.service.spec.CryptoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/crypto")
public class CryptoApiController implements CryptoApi {

  private final CryptoService cryptoService;

  @RateLimited
  public ResponseEntity<List<Crypto>> getSortedCryptosByNormalizedRange() {
    return new ResponseEntity<>(cryptoService.calculateNormalizedRangesAndSort(), HttpStatus.OK);
  }

  @RateLimited
  public ResponseEntity<Map<String, List<Crypto>>> getStatisticsForMonthAndYear(
          @PathVariable int year, @PathVariable int month) throws InvalidDateException {
    return new ResponseEntity<>(cryptoService.getStatisticsByMonthAndYear(year, month), HttpStatus.OK);
  }

  @RateLimited
  public ResponseEntity<List<Pair<String, Crypto>>> getStatisticsForCrypto(@PathVariable Symbol crypto) {
    return new ResponseEntity<>(cryptoService.getStatisticsForCertainCrypto(crypto), HttpStatus.OK);
  }

  @RateLimited
  public ResponseEntity<Crypto> getHighestNormalizedRange(@RequestBody LocalDate date) {
    return new ResponseEntity<>(cryptoService.getCryptoWithHighestNormalizedRange(date), HttpStatus.OK);
  }
}
