package org.example.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.service.impl.util.CryptoServiceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
  @Value("${cache.expire.interval.minutes}")
  private Integer expireInterval;

  @Value("${cache.maximum.entries}")
  private Integer maxEntries;

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeineCacheBuilder());
    cacheManager.setCacheNames(List.of(CryptoServiceUtil.CACHE_NAME));
    return cacheManager;
  }

  public Caffeine<Object, Object> caffeineCacheBuilder() {
    return com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
            .expireAfterWrite(expireInterval, TimeUnit.MINUTES)
            .maximumSize(maxEntries)
            .recordStats();
  }
}