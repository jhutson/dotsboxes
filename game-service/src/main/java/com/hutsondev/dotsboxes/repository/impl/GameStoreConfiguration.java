package com.hutsondev.dotsboxes.repository.impl;

import com.hutsondev.dotsboxes.repository.GameStore;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameStoreConfiguration {

  private final ApplicationContext context;

  public GameStoreConfiguration(@NonNull ApplicationContext context) {
    this.context = context;
  }

  @Bean
  public GameStore gameStore(
      @Value("${com.hutsondev.gamestore}") String gameStoreQualifier) {
    return context.getBean(gameStoreQualifier, GameStore.class);
  }
}
