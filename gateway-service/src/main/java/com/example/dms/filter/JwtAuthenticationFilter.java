package com.example.dms.filter;

import com.example.dms.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // If you need to skip some endpoints (like /eureka), do it here
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/eureka") || path.startsWith("/user-service/api/auth/")) {
            return chain.filter(exchange);
        }

        // Parse token
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        String token = jwtUtils.parseBearerToken(authHeader);

        if (token == null || !jwtUtils.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract details
        String username = jwtUtils.extractUsername(token);
        String role = jwtUtils.extractRoles(token);

        // Create Authentication object
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null, // No credentials since it's JWT
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)) // Convert roles to authorities
                );

        // Add Authentication to SecurityContext
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
