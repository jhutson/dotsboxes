package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.events.impl.SnsGameEventPublisher;
import lombok.NonNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class GameEventPublisherConfiguration {

  private final ApplicationContext context;

  public GameEventPublisherConfiguration(@NonNull ApplicationContext context) {
    this.context = context;
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public GameEventPublisher gameEventPublisher() {
    return context.getBean(SnsGameEventPublisher.class);
  }
}
