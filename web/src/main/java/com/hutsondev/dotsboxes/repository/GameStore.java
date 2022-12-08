package com.hutsondev.dotsboxes.repository;

import com.hutsondev.dotsboxes.model.GameSession;
import java.util.Optional;

public interface GameStore {

  GameSession create(int rowCount, int columnCount, String playerOneId, String playerTwoId);

  Optional<GameSession> get(String gameId);

  boolean remove(String gameId);
}
