package com.hutsondev.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutsondev.dynamodb.configuration.DynamoDbAutoConfiguration;
import com.hutsondev.dynamodb.repository.GameSessionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@SpringBootTest(classes = {DynamoDbAutoConfiguration.class})
public class GameSessionTest {

  private DynamoDbTable<GameSessionEntity> gameSessions;

  @Test
  void roundTripTest() {
    GameSessionEntity expected = new GameSessionEntity();
    expected.setId("test1");
    expected.setGame(new byte[]{(byte) 0xBA, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF});
    expected.setPlayerOneId("p1");
    expected.setPlayerTwoId("p2");

    gameSessions.putItem(expected);
    GameSessionEntity actual = gameSessions.getItem(Key.builder().partitionValue("test1").build());

    expected.setVersion(1);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
