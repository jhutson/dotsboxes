package com.hutsondev.dotsboxes.configuration;

import com.hutsondev.dotsboxes.repository.impl.GameSessionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Configuration
public class GameStoreConfiguration {

  @Bean
  @Lazy
  public DynamoDbTable<GameSessionEntity> gameSessions(DynamoDbEnhancedClient dynamoDbClient,
      @Value("${com.hutsondev.dynamodb.table.game-sessions}") String tableName) {
    return dynamoDbClient.table(tableName, TableSchema.fromBean(GameSessionEntity.class));
  }
}
