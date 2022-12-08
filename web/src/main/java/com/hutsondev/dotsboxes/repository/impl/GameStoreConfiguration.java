package com.hutsondev.dotsboxes.repository.impl;

import com.hutsondev.dotsboxes.repository.GameStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameStoreConfiguration {

  @Bean
  public GameStore gameStore() {
    return new SingleGameStore();
  }
}
