package com.hutsondev.dotsboxes.repository.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.model.GameSession;
import com.hutsondev.dotsboxes.service.GameState;
import com.hutsondev.dotsboxes.service.StateConverter;
import com.hutsondev.dotsboxes.repository.GameStore;
import com.hutsondev.dynamodb.repository.GameSessionEntity;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Lazy
@Component("dynamodb")
public class DynamoDbGameStore implements GameStore {

  private final DynamoDbTable<GameSessionEntity> gameSessions;

  public DynamoDbGameStore(@NonNull DynamoDbTable<GameSessionEntity> gameSessions) {
    this.gameSessions = gameSessions;
  }

  @Override
  public GameSession create(int rowCount, int columnCount, String playerOneId, String playerTwoId) {
    Game game = new Game(rowCount, columnCount);
    int initialVersion = 1;

    GameSessionEntity entity = new GameSessionEntity();
    entity.setId(UUID.randomUUID().toString());
    entity.setPlayerOneId(playerOneId);
    entity.setPlayerTwoId(playerTwoId);
    entity.setGame(StateConverter.toGameState(game, initialVersion).toByteArray());

    gameSessions.putItem(entity);

    return new GameSession(
        game,
        entity.getId(),
        initialVersion,
        entity.getPlayerOneId(),
        entity.getPlayerTwoId()
    );
  }

  @Override
  public Optional<GameSession> get(String gameId) {
    Key key = Key.builder().partitionValue(gameId).build();
    GameSessionEntity entity = gameSessions.getItem(key);

    if (entity == null) {
      return Optional.empty();
    }

    Game game;

    try {
      game = StateConverter.toGame(GameState.parseFrom(entity.getGame()));
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }

    return Optional.of(
        new GameSession(
            game,
            entity.getId(),
            entity.getVersion(),
            entity.getPlayerOneId(),
            entity.getPlayerTwoId()
        )
    );
  }

  @Override
  public GameSession update(GameSession gameSession) {
    GameSessionEntity entity = new GameSessionEntity();
    entity.setId(gameSession.gameId());
    entity.setGame(
        StateConverter.toGameState(
            gameSession.game(), gameSession.sequenceNumber() + 1).toByteArray()
    );
    entity.setVersion(gameSession.sequenceNumber());
    entity.setPlayerOneId(gameSession.playerOneId());
    entity.setPlayerTwoId(gameSession.playerTwoId());

    gameSessions.updateItem(entity);

    return gameSession.incrementSequenceNumber();
  }

  @Override
  public boolean remove(String gameId) {
    Key key = Key.builder().partitionValue(gameId).build();
    GameSessionEntity entity = gameSessions.deleteItem(key);
    return entity != null;
  }
}
