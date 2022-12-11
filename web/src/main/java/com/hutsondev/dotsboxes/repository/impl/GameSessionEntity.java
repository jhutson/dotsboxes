package com.hutsondev.dotsboxes.repository.impl;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
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


  @Getter(onMethod_ = {@DynamoDbAttribute("p1")})
  @Setter
  private String playerOneId;

  @Getter(onMethod_ = {@DynamoDbAttribute("p2")})
  @Setter
  private String playerTwoId;

  @Getter(onMethod_ = {@DynamoDbAttribute("v"), @DynamoDbVersionAttribute})
  @Setter
  private int version;

  public static GameSessionEntity withKey(String id) {
    GameSessionEntity entity = new GameSessionEntity();
    entity.setId(id);
    return entity;
  }
}
