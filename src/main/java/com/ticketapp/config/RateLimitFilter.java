package com.ticketapp.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private ProxyManager<byte[]> proxyManager;

    @Autowired
    private RateLimitConfig config;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // YENİ EKLENEN: CORS Preflight (OPTIONS) isteklerini limitten muaf tutuyoruz!
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Kullanıcının IP adresini alıyoruz ve byte dizisine çeviriyoruz (Lettuce formatı)
        String ip = request.getRemoteAddr();
        System.out.println(":rotating_light: RATE LIMIT KONTROLÜ - Gelen IP: " + ip);
        byte[] key = ip.getBytes(StandardCharsets.UTF_8);

        // IP adresine ait kovayı (bucket) Redis'ten getiriyoruz
        Bucket bucket = proxyManager.builder().build(key, config.bucketConfiguration());

        if (bucket.tryConsume(1)) {
            // Jeton var! İsteğin geçmesine izin ver
            filterChain.doFilter(request, response);
        } else {
            // React tarafında "Network Error" görmemek için bu 2 satır şart:
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            // Jeton bitti! 429 Too Many Requests hatasını bas
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"rateLimit\"}");
        }
    }
}