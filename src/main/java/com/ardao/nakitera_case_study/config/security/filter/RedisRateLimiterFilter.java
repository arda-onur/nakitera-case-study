package com.ardao.nakitera_case_study.config.security.filter;

import com.ardao.nakitera_case_study.service.redis.RedisRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RedisRateLimiterFilter extends OncePerRequestFilter {

    private final RedisRateLimiterService redisRateLimiterService;

    public RedisRateLimiterFilter(RedisRateLimiterService redisRateLimiterService) {
        this.redisRateLimiterService = redisRateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String key = "rate-limit" + request.getRemoteAddr();


        boolean isAllowed = this.redisRateLimiterService.isAllowed(key);

        if(!isAllowed){
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Too many request please wait!");
            return;
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/h2-console") || uri.startsWith("/actuator");
    }
}
