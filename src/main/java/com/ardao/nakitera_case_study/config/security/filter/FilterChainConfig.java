package com.ardao.nakitera_case_study.config.security.filter;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class FilterChainConfig {
    private final MessageSource messageSource;
    private final RedisRateLimiterFilter redisRateLimiterFilter;

    public FilterChainConfig(MessageSource messageSource, RedisRateLimiterFilter redisRateLimiterFilter) {
        this.messageSource = messageSource;
        this.redisRateLimiterFilter = redisRateLimiterFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers("/login",
                                "/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers("/customer/create","/order/match").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .addFilterBefore(redisRateLimiterFilter, BasicAuthenticationFilter.class)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String message = messageSource.getMessage(
                                    "authentication.required.exception",
                                    null,
                                    "Authentication is required.",
                                    request.getLocale()
                            );

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write(message);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String message = messageSource.getMessage(
                                    "access.denied.exception",
                                    null,
                                    "You do not have permission to access this resource.",
                                    request.getLocale()
                            );

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write(message);
                        }))

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
