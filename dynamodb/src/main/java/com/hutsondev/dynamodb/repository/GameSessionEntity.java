package com.hutsondev.dynamodb.repository;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Data
public class GameSessionEntity {

  @Getter(onMethod_ = {@DynamoDbPartitionKey})
  private String id;

  private byte[] game;

  @Getter(onMethod_ = {@DynamoDbAttribute("p1")})
  private String playerOneId;

  @Getter(onMethod_ = {@DynamoDbAttribute("p2")})
  private String playerTwoId;

  @Getter(onMethod_ = {@DynamoDbAttribute("v"), @DynamoDbVersionAttribute})
  private Integer version;
}
