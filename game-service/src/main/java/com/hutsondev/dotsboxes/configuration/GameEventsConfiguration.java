package com.hutsondev.dotsboxes.configuration;

import com.hutsondev.dotsboxes.events.impl.GameEvents;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class GameEventsConfiguration {

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public GameEvents gameEventPublisher() {
    return new GameEvents();
  }
}
