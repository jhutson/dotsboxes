package com.hutsondev.dynamodb;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

// @Component
public class CreateDynamoDbTablesRunner implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(CreateDynamoDbTablesRunner.class);

  private final List<DynamoDbTable<?>> tables;

  public CreateDynamoDbTablesRunner(List<DynamoDbTable<?>> tables) {
    this.tables = tables;
  }

  @Override
  public void run(String... args) throws Exception {
    for (DynamoDbTable<?> table : tables) {
      logger.info("Creating table {}", table.tableName());

      try {
        table.createTable();
      } catch (ResourceInUseException e) {
        logger.info("Table already exists");
      }
    }
    logger.info("Done");
  }
}