package com.hutsondev.dotsboxes.events.impl;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClientBuilder;

@Lazy
@Configuration
public class SnsClientConfiguration {

  @Bean
  public SnsAsyncClient getSnsAsyncClient(@Value("${com.hutsondev.sns.custom-endpoint-uri:''}") String customEndpointUri) {
    SnsAsyncClientBuilder builder = SnsAsyncClient.builder();

    if (customEndpointUri.length() > 0) {
      builder.endpointOverride(URI.create(customEndpointUri));
    }

    return builder.build();
  }
}
