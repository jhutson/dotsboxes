package com.hutsondev.dynamodb;

import com.hutsondev.dynamodb.configuration.DynamoDbAutoConfiguration;
import com.hutsondev.dynamodb.repository.GameSessionEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@SpringBootTest(classes = {DynamoDbAutoConfiguration.class})
public class DynamoDbContextTest {

  private DynamoDbTable<GameSessionEntity> gameSessions;

  @Test
  void contextLoads() {
    Assertions.assertNotNull(gameSessions);
  }
}
