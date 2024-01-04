package org.example.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.example.exception.InvalidDateException;
import org.example.model.Crypto;
import org.example.model.Symbol;
import org.example.service.impl.util.CryptoServiceUtil;
import org.example.service.spec.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CryptoServiceImpl implements CryptoService {

    private final CacheManager cacheManager;
    private final ResourceLoader resourceLoader;
    private static final Logger logger = LoggerFactory.getLogger(CryptoServiceImpl.class);

    public List<Crypto> readCSVFiles(Symbol... filePaths) {
        List<Crypto> cryptos = new ArrayList<>();
        for (Symbol filePath : filePaths) {
            try {
                cryptos.addAll(readCryptoFromCsv(filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return cryptos;
    }

    public Map<String, List<Crypto>> getStatisticsByMonthAndYear(Integer year, Integer month) throws InvalidDateException {
        validateYearMonthParameters(year, month);
        Map<String, List<Crypto>> cryptoMap = new HashMap<>();
        cryptoMap.put("OLDEST", getCryptoForYearAndMonth(year, month, CryptoServiceUtil.COMPARING_TIMESTAMP));
        cryptoMap.put("NEWEST", getCryptoForYearAndMonth(year, month, CryptoServiceUtil.COMPARING_TIMESTAMP.reversed()));
        cryptoMap.put("MINIMUM", getCryptoForYearAndMonth(year, month, CryptoServiceUtil.COMPARING_PRICE));
        cryptoMap.put("MAXIMUM", getCryptoForYearAndMonth(year, month, CryptoServiceUtil.COMPARING_PRICE.reversed()));
        return cryptoMap;
    }

    public List<Pair<String, Crypto>> getStatisticsForCertainCrypto(Symbol crypto) {
        List<Pair<String, Crypto>> cryptos = new ArrayList<>();
        cryptos.add(Pair.of(crypto.name() + "-oldest", getCryptoByType(crypto, CryptoServiceUtil.COMPARING_TIMESTAMP)));
        cryptos.add(Pair.of(crypto.name() + "-newest", getCryptoByType(crypto, CryptoServiceUtil.COMPARING_TIMESTAMP.reversed())));
        cryptos.add(Pair.of(crypto.name() + "-min", getCryptoByType(crypto, CryptoServiceUtil.COMPARING_PRICE)));
        cryptos.add(Pair.of(crypto.name() + "-max", getCryptoByType(crypto, CryptoServiceUtil.COMPARING_PRICE.reversed())));
        return cryptos;
    }

    public Crypto getCryptoWithHighestNormalizedRange(LocalDate date) {
        List<Crypto> cryptos = calculateNormalizedRangesAndSort();
        return cryptos.stream().filter(crypto -> crypto.getTimestamp().toLocalDate().equals(date)).peek(System.out::println).findFirst().orElseThrow(() -> new DateTimeException("Invalid DateTime"));
    }

    public List<Double> getAllPrices(Symbol... filePaths) throws CsvValidationException {
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
                throw new CsvValidationException(e.getMessage());
            }
        }
        return columnValues;
    }

    public List<Crypto> calculateNormalizedRangesAndSort() {
        List<Crypto> cryptoList = readCSVFiles(Symbol.values());
        enhanceCryptoWithNormalizeValue(cryptoList);
        return getSortedByNormalizedValue(cryptoList);
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

    public List<Crypto> readCryptoFromCsv(Symbol filePath) throws IOException {
        Cache cache = cacheManager.getCache(CryptoServiceUtil.CACHE_NAME);
        Resource resource = getResource(filePath);
        if (cache != null) {
            if (isKeyInCache(cache, filePath.name())) {
                return getCryptoFromCache(filePath, cache);
            } else {
                return getCryptosFromResource(filePath, resource, cache);
            }
        }
        throw new RuntimeException("Cache in null");
    }

    private static ArrayList<Crypto> getCryptosFromResource(Symbol filePath, Resource resource, Cache cache) throws IOException {
        logger.info("Loading from resource " + filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            CsvToBean<Crypto> csvToBean = new CsvToBeanBuilder<Crypto>(reader).withSkipLines(1).withType(Crypto.class).withIgnoreLeadingWhiteSpace(true).build();
            ArrayList<Crypto> cryptoListFromResource = new ArrayList<>(csvToBean.parse());
            cache.put(filePath.name(), cryptoListFromResource);
            return cryptoListFromResource;
        }
    }

    private static List<Crypto> getCryptoFromCache(Symbol filePath, Cache cache) {
        logger.info("Loading from cache " + filePath);
        Cache.ValueWrapper valueWrapper = cache.get(filePath.name());
        return (List<Crypto>) valueWrapper.get();
    }

    private static List<Crypto> getSortedByNormalizedValue(List<Crypto> cryptoList) {
        return cryptoList.parallelStream().sorted(Comparator.comparingDouble(Crypto::getNormalizedRange).reversed()).collect(Collectors.toList());
    }

    private static Double getMaxPriceForSymbol(List<Crypto> cryptoList, Symbol symbol) {
        return cryptoList.stream().filter(c -> c.getSymbol().equals(symbol)).mapToDouble(Crypto::getPrice).max().orElse(0);
    }

    private static Double getMinPriceForSymbol(List<Crypto> cryptoList, Symbol symbol) {
        return cryptoList.stream().filter(c -> c.getSymbol().equals(symbol)).mapToDouble(Crypto::getPrice).min().orElse(0);
    }

    private List<Crypto> getCryptoForYearAndMonth(Integer year, Integer month, Comparator<Crypto> comparator) {
        List<Crypto> resultCryptos = new ArrayList<>();

        for (Symbol filePath : Symbol.values()) {
            List<Crypto> cryptoList = readCSVFiles(filePath);

            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            resultCryptos.addAll(filterByYearAndMonth(cryptoList, startOfMonth, endOfMonth, comparator));
        }

        return resultCryptos;
    }

    private List<Crypto> filterByYearAndMonth(List<Crypto> cryptoList, LocalDateTime startOfMonth, LocalDateTime endOfMonth, Comparator<Crypto> comparator) {
        return cryptoList.stream().filter(crypto -> isWithinMonth(crypto.getTimestamp(), startOfMonth, endOfMonth)).min(comparator).stream().collect(Collectors.toList());
    }

    private void enhanceCryptoWithNormalizeValue(List<Crypto> cryptoList) {
        cryptoList.forEach(crypto -> {
            double maxPrice = getMaxPriceForSymbol(cryptoList, crypto.getSymbol());
            double minPrice = getMinPriceForSymbol(cryptoList, crypto.getSymbol());

            double normalizedRange = (maxPrice - minPrice) / minPrice;
            crypto.setNormalizedRange(normalizedRange);
        });
    }

    private void validateYearMonthParameters(Integer year, Integer month) throws InvalidDateException {
        if (year == null || month == null || year < 0 || month < 1 || month > 12) {
            throw new InvalidDateException("Invalid year or month");
        }
    }

    public boolean isKeyInCache(Cache cache, Object key) {
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            return valueWrapper != null && valueWrapper.get() != null;
        }
        return false;
    }
}
