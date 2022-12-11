package com.hutsondev.dotsboxes.model;

import com.hutsondev.dotsboxes.core.Game;
import com.hutsondev.dotsboxes.core.Player;
import java.util.Objects;
import java.util.Optional;

public record GameSession(
    Game game,
    String gameId,
    int sequenceNumber,
    String playerOneId,
    String playerTwoId) {

  public GameSession {
    Objects.requireNonNull(game);
    Objects.requireNonNull(gameId);
    Objects.requireNonNull(playerOneId);
    Objects.requireNonNull(playerTwoId);
  }

  public boolean isPlayerTurn(String playerId) {
    Player currentPlayer = game.getCurrentPlayer();
    if (currentPlayer == Player.ONE && playerOneId.equals(playerId)) {
      return true;
    }
    return currentPlayer == Player.TWO && playerTwoId.equals(playerId);
  }

  public Optional<Player> getPlayerIndex(String playerId) {
    if (playerOneId.equals(playerId)) {
      return Optional.of(Player.ONE);
    } else if (playerTwoId.equals(playerId)) {
      return Optional.of(Player.TWO);
    }
    return Optional.empty();
  }

  public GameSession incrementSequenceNumber() {
    return new GameSession(
        this.game(),
        this.gameId(),
        this.sequenceNumber() + 1,
        this.playerOneId(),
        this.playerTwoId()
    );
  }
}
