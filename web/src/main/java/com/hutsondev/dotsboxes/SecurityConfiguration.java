package com.hutsondev.dotsboxes;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfiguration {

  // TODO: Re-enable security authorization
  // TODO: Re-enable CSRF
  @Bean
  public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
    return http.authorizeExchange()
        .anyExchange()
        .permitAll()
        .and()
        .csrf(CsrfSpec::disable)
        .build();
  }
}