package com.ticketapp.config;

import com.ticketapp.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private ProxyManager<byte[]> proxyManager;

    @Before("@annotation(rateLimit)")
    public void doRateLimit(RateLimit rateLimit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteAddr();

        // Her metot için ayrı kilit (Login için ayrı, Bilet için ayrı)
        String keyStr = ip + "-" + request.getRequestURI();
        byte[] key = keyStr.getBytes(StandardCharsets.UTF_8);

        BucketConfiguration specificConfig = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(rateLimit.capacity(),
                        Refill.intervally(rateLimit.capacity(), Duration.ofMinutes(1))))
                .build();

        // LİMİT KONTROLÜ (İşte burayı tam bu şekilde bağlıyoruz aga):
        io.github.bucket4j.ConsumptionProbe probe = proxyManager.builder()
                .build(key, specificConfig)
                .tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            // Beklemesi gereken süreyi saniyeye çeviriyoruz
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000L;
            if (waitForRefill == 0) waitForRefill = 1;

            throw new RateLimitException("rateLimit", waitForRefill);
        }
    }
}