package com.hutsondev.dynamodb.configuration;

import com.hutsondev.dynamodb.repository.GameSessionEntity;
import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@EnableConfigurationProperties(DynamoDbProperties.class)
public class DynamoDbAutoConfiguration {

  @Bean
  @Lazy
  public DynamoDbEnhancedClient dynamoDbClient(DynamoDbProperties properties) {
    String endpointUrl = properties.getEndpointUrl();
    DynamoDbClient client;

    if (endpointUrl.length() == 0) {
      client = DynamoDbClient.create();
    } else {
      client = DynamoDbClient.builder().endpointOverride(URI.create(endpointUrl)).build();
    }

    return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
  }

  @Bean
  @Lazy
  public DynamoDbTable<GameSessionEntity> gameSessions(DynamoDbEnhancedClient dynamoDbClient,
      DynamoDbProperties properties) {

    String tableName = properties.getTable().getGameSessions();
    return dynamoDbClient.table(tableName, TableSchema.fromBean(GameSessionEntity.class));
  }
}
