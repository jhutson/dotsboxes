package com.hutsondev.dotsboxes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hutsondev.dotsboxes.core.Board;
import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.model.GameSession;
import com.hutsondev.dotsboxes.repository.GameStore;
import com.hutsondev.dotsboxes.repository.impl.DynamoDbGameStore;
import com.hutsondev.dotsboxes.service.StateConverter;
import com.hutsondev.dynamodb.configuration.DynamoDbAutoConfiguration;
import com.hutsondev.dynamodb.repository.GameSessionEntity;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@SpringBootTest(classes = {DynamoDbAutoConfiguration.class})
public class DynamoDbGameStoreTest {

  private final Random random = new Random(257810);

  private DynamoDbTable<GameSessionEntity> gameSessions;

  private GameStore gameStore;

  @BeforeEach
  public void setupTestCase() {
    gameStore = new DynamoDbGameStore(gameSessions);
  }

  @Test
  public void create() {
    int rowCount = random.nextInt(1, 10);
    int columnCount = random.nextInt(1, 10);
    String playerOne = "test-player-one";
    String playerTwo = "test-player-two";

    GameSession session = gameStore.create(rowCount, columnCount, playerOne, playerTwo);

    GameSession expectedSession = new GameSession(
        new Game(new Board(rowCount, columnCount)),
        "unused",
        1,
        playerOne,
        playerTwo
    );

    assertThat(session)
        .usingRecursiveComparison()
        .ignoringFields("gameId")
        .isEqualTo(expectedSession);

    GameSessionEntity expectedSessionEntity = new GameSessionEntity();
    expectedSessionEntity.setId(session.gameId());
    expectedSessionEntity.setPlayerOneId(playerOne);
    expectedSessionEntity.setPlayerTwoId(playerTwo);
    expectedSessionEntity.setVersion(1);

    assertThat(gameSessions.getItem(Key.builder().partitionValue(session.gameId()).build()))
        .usingRecursiveComparison()
        .ignoringFields("game")
        .isEqualTo(expectedSessionEntity);
  }

  @Test
  public void update() {
    GameSessionEntity expectedSessionEntity = new GameSessionEntity();
    expectedSessionEntity.setId(UUID.randomUUID().toString());
    expectedSessionEntity.setGame(new byte[]{(byte) 0x00});
    expectedSessionEntity.setPlayerOneId("test-player-one");
    expectedSessionEntity.setPlayerTwoId("test-player-two");
    gameSessions.putItem(expectedSessionEntity);

    Game game = new Game(4, 5);
    game.markLine(1, 1);
    game.markLine(0, 3);
    GameSession session = new GameSession(
        game,
        expectedSessionEntity.getId(),
        1,
        expectedSessionEntity.getPlayerOneId(),
        expectedSessionEntity.getPlayerTwoId());

    assertThat(gameStore.update(session))
        .usingRecursiveComparison()
        .isEqualTo(session.incrementSequenceNumber());

    expectedSessionEntity.setVersion(session.sequenceNumber() + 1);
    expectedSessionEntity.setGame(
        StateConverter.toGameState(game, expectedSessionEntity.getVersion()).toByteArray());

    assertThat(
        gameSessions.getItem(Key.builder().partitionValue(expectedSessionEntity.getId()).build()))
        .usingRecursiveComparison()
        .isEqualTo(expectedSessionEntity);
  }

  @Test
  public void remove() {
    GameSessionEntity sessionEntity = new GameSessionEntity();
    sessionEntity.setId(UUID.randomUUID().toString());
    sessionEntity.setGame(new byte[]{(byte) 0x00});
    sessionEntity.setPlayerOneId("test-player-one");
    sessionEntity.setPlayerTwoId("test-player-two");

    assertThat(gameStore.remove(sessionEntity.getId())).isFalse();
    gameSessions.putItem(sessionEntity);
    assertThat(gameStore.remove(sessionEntity.getId())).isTrue();

    assertThat(gameSessions.getItem(Key.builder().partitionValue(sessionEntity.getId()).build()))
        .isNull();
  }

  @Test
  public void get_with_valid_game_bytes() {
    Game game = new Game(4, 5);

    GameSessionEntity expectedSessionEntity = new GameSessionEntity();
    expectedSessionEntity.setId(UUID.randomUUID().toString());
    expectedSessionEntity.setGame(StateConverter.toGameState(game, 1).toByteArray());
    expectedSessionEntity.setPlayerOneId("test-player-one");
    expectedSessionEntity.setPlayerTwoId("test-player-two");
    gameSessions.putItem(expectedSessionEntity);
    expectedSessionEntity.setVersion(1);

    assertThat(gameStore.get(expectedSessionEntity.getId()))
        .usingRecursiveComparison()
        .isEqualTo(
            Optional.of(
                new GameSession(
                    game,
                    expectedSessionEntity.getId(),
                    expectedSessionEntity.getVersion(),
                    expectedSessionEntity.getPlayerOneId(),
                    expectedSessionEntity.getPlayerTwoId()
                )));
  }

  @Test
  public void get_with_missing_item() {
    assertThat(gameStore.get(UUID.randomUUID().toString())).isNotPresent();
  }

  @Test
  public void get_with_invalid_game_bytes() {
    GameSessionEntity sessionEntity = new GameSessionEntity();
    sessionEntity.setId(UUID.randomUUID().toString());
    sessionEntity.setGame(new byte[]{(byte) 0xBA, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF});
    sessionEntity.setPlayerOneId("test-player-one");
    sessionEntity.setPlayerTwoId("test-player-two");
    gameSessions.putItem(sessionEntity);

    assertThatThrownBy(() -> gameStore.get(sessionEntity.getId()))
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(InvalidProtocolBufferException.class);
  }
}
