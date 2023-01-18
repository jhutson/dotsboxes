package com.hutsondev.dotsboxes.repository.impl;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Lazy
@Configuration
public class DynamoDbConfiguration {

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public DynamoDbEnhancedClient dynamoDbClient(
      @Value("${com.hutsondev.dynamodb.endpoint-url}") String endpointUrl) {

    DynamoDbClient client;

    if (endpointUrl == null) {
      client = DynamoDbClient.create();
    } else {
      client = DynamoDbClient.builder().endpointOverride(URI.create(endpointUrl)).build();
    }

    return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public DynamoDbTable<GameSessionEntity> gameSessions(DynamoDbEnhancedClient dynamoDbClient,
      @Value("${com.hutsondev.dynamodb.table.game-sessions}") String gameSessionsTableName) {
    return dynamoDbClient.table(gameSessionsTableName,
        TableSchema.fromBean(GameSessionEntity.class));
  }
}
