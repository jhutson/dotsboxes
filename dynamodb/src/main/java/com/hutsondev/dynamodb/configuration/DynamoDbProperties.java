package com.hutsondev.dynamodb.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.hutsondev.dynamodb")
public class DynamoDbProperties {

  @Getter
  @Setter
  private String endpointUrl;

  @Getter
  private final Table table = new Table();

  public static class Table {

    @Getter
    @Setter
    private String gameSessions;

    @Getter
    @Setter
    private String players;
  }
}
