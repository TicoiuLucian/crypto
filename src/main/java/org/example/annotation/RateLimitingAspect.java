package org.example.annotation;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.exception.ErrorMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class RateLimitingAspect {

  @Value("${rate.limit.capacity}")
  private Integer capacity;

  @Value("${rate.limit.refill.tokens}")
  private Integer refillTokens;

  @Value("${rate.limit.refill.interval.seconds}")
  private Integer intervalRefill;

  private Bucket bucket;

  @PostConstruct
  public void init() {
    Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(refillTokens, Duration.ofSeconds(intervalRefill)));
    this.bucket = Bucket.builder().addLimit(limit).build();
  }

  @Around("@annotation(RateLimited)")
  public Object rateLimitAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    if (bucket.tryConsume(1)) {
      return joinPoint.proceed();
    }
    return handleTooManyRequests();
  }

  private ResponseEntity<ErrorMessage> handleTooManyRequests() {
    HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
    String errorMessage = String.format("Too many requests. Maximum is %d per %d seconds", capacity, intervalRefill);
    return ResponseEntity.status(status).body(new ErrorMessage(errorMessage));
  }
}
