package com.hutsondev.dotsboxes.repository.impl;

import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.model.GameSession;
import com.hutsondev.dotsboxes.repository.GameStore;
import java.util.Optional;

public class SingleGameStore implements GameStore {

  private static GameSession CURRENT_GAME = null;
  private static final String GAME_ID = "A33DFDFF-A3C0-4F7F-B4B2-9664E78D111B";

  @Override
  public GameSession create(int rowCount, int columnCount, String playerOneId, String playerTwoId) {
    CURRENT_GAME = new GameSession(
        new Game(rowCount, columnCount),
        GAME_ID,
        playerOneId,
        playerTwoId
    );

    return CURRENT_GAME;
  }

  @Override
  public Optional<GameSession> get(String gameId) {
    if (GAME_ID.equalsIgnoreCase(gameId) && CURRENT_GAME != null) {
      return Optional.of(CURRENT_GAME);
    }

    return Optional.empty();
  }

  @Override
  public boolean remove(String gameId) {
    Optional<GameSession> gameSession = get(gameId);
    if (gameSession.isPresent()) {
      CURRENT_GAME = null;
      return true;
    }
    return false;
  }
}
