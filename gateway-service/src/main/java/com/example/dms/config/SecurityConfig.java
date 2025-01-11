package com.example.dms.config;


import com.example.dms.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
                                                       JwtAuthenticationFilter jwtAuthGlobalFilter) {

        return http
                // Disable CSRF for stateless services
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                // Match any request and apply the rules
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/user-service/api/auth/**").permitAll()  // eureka endpoints allowed
                        .anyExchange().authenticated()           // all others require auth
                )

                // Insert our custom JWT filter
                // SecurityWebFiltersOrder.AUTHENTICATION ensures it runs before standard Auth
                .addFilterBefore(jwtAuthGlobalFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // We do not need httpBasic or formLogin unless you want them
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .build();
    }
}
