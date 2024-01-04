package org.example.service.spec;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.exception.InvalidDateException;
import org.example.model.Crypto;
import org.example.model.Symbol;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Recommendation service
 */
public interface CryptoService {

  /**
   * Read all cryptos from csv files
   *
   * @param symbols: names of the crypto currency
   * @return List of {@link Crypto}
   */
  List<Crypto> readCSVFiles(Symbol... symbols) throws IOException;

  /**
   * Compute a map with statistic name and the list of crypto for it
   *
   * @param year
   * @param month
   * @return Map<String, List < { @ link Crypto }> containing statistic name(min/max/newest/oldest} and the list of
   * {@link Crypto}
   */
  Map<String, List<Crypto>> getStatisticsByMonthAndYear(Integer year, Integer month) throws InvalidDateException;

  /**
   * Compute a {@link List} of {@link Pair}s of statistic name and desired {@link Crypto} statistic name and the list
   * of crypto for it
   *
   * @param symbol: names of the crypto currency
   * @return {@link List} of {@link Pair}s
   */
  List<Pair<String, Crypto>> getStatisticsForCertainCrypto(Symbol symbol);

  /**
   * Get all prices for one or more symbols
   *
   * @param symbols: names of the crypto currency
   * @return {@link List} of Double
   */
  List<Double> getAllPrices(Symbol... symbols) throws CsvValidationException;

  /**
   * Calculates and sort all cruyptos by normalized ranges
   *
   * @return {@link List} of {@link Crypto} sorted by normalized ranges
   */
  List<Crypto> calculateNormalizedRangesAndSort();

  /**
   * Calculate and returns the crypto with highest normalized range for a certain {@link LocalDate}
   *
   * @param date for which the search will occur
   * @return {@link Crypto} with highest normalized range
   */
  Crypto getCryptoWithHighestNormalizedRange(LocalDate date);
}
