package com.hutsondev.dynamodb.configuration;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@AutoConfiguration
public class DynamoDbAutoConfiguration {

  @Bean
  @Lazy
  public DynamoDbEnhancedClient dynamoDbClient(@Value("${com.hutsondev.dynamodb.endpoint-url:}") String endpointUrl) {
    DynamoDbClient client;

    if (endpointUrl.length() == 0) {
      client = DynamoDbClient.create();
    } else {
      client = DynamoDbClient.builder().endpointOverride(URI.create(endpointUrl)).build();
    }

    return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
  }
}
