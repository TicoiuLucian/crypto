package org.example.service.impl.util;

import org.example.model.Crypto;

import java.util.Comparator;

public class CryptoServiceUtil {

    public static final Comparator<Crypto> COMPARING_TIMESTAMP = Comparator.comparing(Crypto::getTimestamp);
    public static final Comparator<Crypto> COMPARING_PRICE = Comparator.comparing(Crypto::getPrice);
    public static final String CACHE_NAME = "cryptoData";
}
