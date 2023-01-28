package com.hutsondev.authservice.configuration;

import com.hutsondev.authservice.repository.UserEntity;
import java.net.URI;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfiguration {

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public DynamoDbEnhancedClient dynamoDbClient(
      @NonNull @Value("${com.hutsondev.dynamodb.endpoint-url:}") String endpointUrl) {

    DynamoDbClient client;

    if (endpointUrl.length() == 0) {
      client = DynamoDbClient.create();
    } else {
      client = DynamoDbClient.builder().endpointOverride(URI.create(endpointUrl)).build();
    }

    return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public DynamoDbTable<UserEntity> users(DynamoDbEnhancedClient dynamoDbClient,
      @Value("${com.hutsondev.dynamodb.table.users}") String tableName) {
    return dynamoDbClient.table(tableName, TableSchema.fromBean(UserEntity.class));
  }
}
