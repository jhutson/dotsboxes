package com.hutsondev.dotsboxes.model;

import com.hutsondev.dotsboxes.proto.TurnResponse;

public record TurnEvent(String gameId, TurnResponse turnResponse) {

}
