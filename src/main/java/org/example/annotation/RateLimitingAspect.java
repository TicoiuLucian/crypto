package org.example.annotation;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class RateLimitingAspect {

    private final Bucket bucket;

    public RateLimitingAspect() {
        Bandwidth limit = Bandwidth.classic(1, Refill.greedy(1, Duration.ofSeconds(10)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
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
        return ResponseEntity.status(status).body(new ErrorMessage("Too many requests/Only 1 request per 20 seconds allowed"));
    }
}
