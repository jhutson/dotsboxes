package com.hutsondev.dotsboxes.events;

import com.hutsondev.dotsboxes.proto.TurnResponse;

public interface GameEventPublisher {

  public void publishTurn(String gameId, TurnResponse turnResponse);
}
