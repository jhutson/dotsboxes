package com.hutsondev.dotsboxes.repository.impl;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class GameSessionEntity {

  @Getter(onMethod_ = {@DynamoDbPartitionKey})
  @Setter
  private String id;

  @Getter
  @Setter
  private byte[] game;

  @Getter
  @Setter
  private String playerOneId;

  @Getter
  @Setter
  private String playerTwoId;

  @Getter(onMethod_ = {@DynamoDbVersionAttribute})
  @Setter
  private int version;

  public static GameSessionEntity withKey(String id) {
    GameSessionEntity entity = new GameSessionEntity();
    entity.setId(id);
    return entity;
  }
}
