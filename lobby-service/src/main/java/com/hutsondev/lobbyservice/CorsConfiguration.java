package com.hutsondev.lobbyservice;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class CorsConfiguration implements WebFluxConfigurer {

  private final String allowedOrigin;

  public CorsConfiguration(
      @NonNull @Value("${com.hutsondev.cors.allowed-origin:}") String allowedOrigin) {
    this.allowedOrigin = allowedOrigin;
  }

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {

    if (allowedOrigin.length() > 0) {
      corsRegistry.addMapping("/**")
          .allowedOrigins(allowedOrigin)
          .maxAge(3600);
    }
  }
}
