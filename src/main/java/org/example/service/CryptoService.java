package org.example.service;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.example.model.Crypto;
import org.example.model.Symbol;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CryptoService {

    public static final Comparator<Crypto> COMPARING_TIMESTAMP = Comparator.comparing(Crypto::getTimestamp);
    public static final Comparator<Crypto> COMPARING_PRICE = Comparator.comparing(Crypto::getPrice);

    private final ResourceLoader resourceLoader;

    public List<Crypto> readCSVFiles(Symbol... filePaths) {
        List<Crypto> cryptos = new ArrayList<>();
        for (Symbol filePath : filePaths) {
            try {
                cryptos.addAll(readCryptoFromCsv(getResource(filePath)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return cryptos;
    }

    public Map<String, List<Crypto>> getStatisticsByMonthAndYear(Integer year, Integer month) {
        Map<String, List<Crypto>> cryptoMap = new HashMap<>();
        cryptoMap.put("OLDEST", getCryptoForYearAndMonth(year, month, COMPARING_TIMESTAMP));
        cryptoMap.put("NEWEST", getCryptoForYearAndMonth(year, month, COMPARING_TIMESTAMP.reversed()));
        cryptoMap.put("MINIMUM", getCryptoForYearAndMonth(year, month, COMPARING_PRICE));
        cryptoMap.put("MAXIMUM", getCryptoForYearAndMonth(year, month, COMPARING_PRICE.reversed()));
        return cryptoMap;
    }

    public List<Pair<String, Crypto>> getStatisticsForCertainCrypto(Symbol crypto) {
        List<Pair<String, Crypto>> cryptos = new ArrayList<>();
        cryptos.add(Pair.of(crypto.name() + "-oldest", getCryptoByType(crypto, COMPARING_TIMESTAMP)));
        cryptos.add(Pair.of(crypto.name() + "-newest", getCryptoByType(crypto, COMPARING_TIMESTAMP.reversed())));
        cryptos.add(Pair.of(crypto.name() + "-min", getCryptoByType(crypto, COMPARING_PRICE)));
        cryptos.add(Pair.of(crypto.name() + "-max", getCryptoByType(crypto, COMPARING_PRICE.reversed())));
        return cryptos;
    }

    public List<Double> getAllPrices(Symbol... filePaths) {
        List<Double> columnValues = new ArrayList<>();

        for (Symbol filePath : filePaths) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResource(filePath).getInputStream()))) {
                CSVReader csvReader = new CSVReader(reader);
                csvReader.skip(1);
                String[] nextLine;
                while ((nextLine = csvReader.readNext()) != null) {
                    if (nextLine.length >= 3) {
                        columnValues.add(Double.valueOf(nextLine[2]));
                    }
                }
            } catch (IOException | CsvValidationException e) {
                throw new RuntimeException(e);
            }
        }
        return columnValues;
    }

    private Resource getResource(Symbol filePath) {
        return resourceLoader.getResource("classpath:external/" + filePath + "_values.csv");
    }

    private boolean isWithinMonth(LocalDateTime dateTime, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        return !dateTime.isBefore(startOfMonth) && !dateTime.isAfter(endOfMonth);
    }

    private Crypto getCryptoByType(Symbol crypto, Comparator<Crypto> comparator) {
        return readCSVFiles(crypto).stream().min(comparator).orElseThrow();
    }


    private List<Crypto> getCryptoForYearAndMonth(Integer year, Integer month, Comparator<Crypto> comparator) {
        List<Crypto> resultCryptos = new ArrayList<>();

        for (Symbol filePath : Symbol.values()) {
            List<Crypto> cryptoList = readCSVFiles(filePath);

            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            cryptoList.stream().filter(crypto -> isWithinMonth(crypto.getTimestamp(), startOfMonth, endOfMonth)).min(comparator).ifPresent(resultCryptos::add);
        }

        return resultCryptos;
    }

    private static List<Crypto> readCryptoFromCsv(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            CsvToBean<Crypto> csvToBean = new CsvToBeanBuilder<Crypto>(reader).withSkipLines(1).withType(Crypto.class).withIgnoreLeadingWhiteSpace(true).build();
            return new ArrayList<>(csvToBean.parse());
        }
    }

    public List<Crypto> calculateNormalizedRangesAndSort() {
        List<Crypto> cryptoList = readCSVFiles(Symbol.values());
        enhanceCryptoWithNormalizeValue(cryptoList);
        return getSortedByNormalizedValue(cryptoList);
    }

    private static List<Crypto> getSortedByNormalizedValue(List<Crypto> cryptoList) {
        return cryptoList.stream()
                .sorted(Comparator.comparingDouble(Crypto::getNormalizedRange).reversed())
                .peek(System.out::println)
                .collect(Collectors.toList());
    }

    private void enhanceCryptoWithNormalizeValue(List<Crypto> cryptoList) {
        cryptoList.forEach(crypto -> {
            double maxPrice = cryptoList.stream().filter(c -> c.getSymbol().equals(crypto.getSymbol())).mapToDouble(Crypto::getPrice).max().orElse(0);
            double minPrice = cryptoList.stream().filter(c -> c.getSymbol().equals(crypto.getSymbol())).mapToDouble(Crypto::getPrice).min().orElse(0);

            double normalizedRange = (maxPrice - minPrice) / minPrice;
            crypto.setNormalizedRange(normalizedRange);
        });
    }

    public Crypto getCryptoWithHighestNormalizedRange(LocalDate date) {
        List<Crypto> cryptos = calculateNormalizedRangesAndSort();
        return cryptos.stream().filter(crypto -> crypto.getTimestamp().toLocalDate().equals(date))
                .peek(System.out::println)
                .findFirst().orElseThrow(() -> new DateTimeException("Invalid DateTime"));
    }
}
