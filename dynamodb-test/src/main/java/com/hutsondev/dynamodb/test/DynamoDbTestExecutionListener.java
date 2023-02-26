package com.hutsondev.dynamodb.test;

import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

public class DynamoDbTestExecutionListener implements TestExecutionListener, Ordered {

  private static final Logger logger = LoggerFactory.getLogger(DynamoDbTestExecutionListener.class);

  private static final String TEST_TABLES_ATTRIBUTE = "com.hutsondev.dynamodb.DynamoDbTestExecutionListener.testTables";

  /**
   * Order after DependencyInjectionTestExecutionListener.
   *
   * @return {@code 2050}
   */
  @Override
  public int getOrder() {
    return 2050;
  }

  @Override
  public void prepareTestInstance(TestContext testContext) throws Exception {
    Map<Field, DynamoDbTable<?>> testTables = getTestTables(testContext);
    Class<?> testClass = testContext.getTestClass();

    for (Field f : testClass.getDeclaredFields()) {
      Type type = f.getGenericType();
      if (f.getGenericType() instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getRawType().equals(DynamoDbTable.class)) {
          Class<?> entityClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
          DynamoDbTable<?> table = createTableInstance(testContext, entityClass);

          logger.info("Created DynamoDBTable<{}> instance with table name \"{}\" for test {}",
              entityClass, table.tableName(), testClass);

          f.setAccessible(true);
          f.set(testContext.getTestInstance(), table);

          if (testTables == null) {
            testTables = new HashMap<>();
          }

          testTables.put(f, table);
        }
      }
    }

    if (testTables != null) {
      setTestTables(testContext, testTables);
    }
  }

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    Map<Field, DynamoDbTable<?>> testTables = getTestTables(testContext);

    if (testTables != null) {
      for (DynamoDbTable<?> table : testTables.values()) {
        deleteTable(table);
        table.createTable();
        logger.info("Created DynamoDB table {}", table.tableName());
      }
    }
  }

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    Map<Field, DynamoDbTable<?>> testTables = getTestTables(testContext);

    if (testTables != null) {
      for (DynamoDbTable<?> table : testTables.values()) {
        deleteTable(table);
      }
    }
  }

  private boolean deleteTable(DynamoDbTable<?> table) {
    try {
      table.deleteTable();
      logger.info("Deleted DynamoDB table {}", table.tableName());
      return true;
    } catch (ResourceNotFoundException ignored) {
      // ignore
      return false;
    }
  }

  private Map<Field, DynamoDbTable<?>> getTestTables(TestContext testContext) {
    @SuppressWarnings("unchecked")
    Map<Field, DynamoDbTable<?>> map = (Map<Field, DynamoDbTable<?>>) testContext.getAttribute(
        TEST_TABLES_ATTRIBUTE);
    return map;
  }

  private void setTestTables(TestContext testContext, Map<Field, DynamoDbTable<?>> testTables) {
    testContext.setAttribute(TEST_TABLES_ATTRIBUTE, testTables);
  }

  private DynamoDbEnhancedClient getClient(TestContext testContext) {
    return testContext.getApplicationContext().getBean(DynamoDbEnhancedClient.class);
  }

  private <T> DynamoDbTable<T> createTableInstance(TestContext testContext, Class<T> entityClass) {
    String className = entityClass.getSimpleName();
    if (className.endsWith("Entity")) {
      className = className.substring(0, className.length() - "Entity".length());
    }

    String tableName = String.format("test-%1$s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS%2$tL-%3$d",
        className, Calendar.getInstance(), testContext.getTestInstance().hashCode());
    return getClient(testContext).table(tableName, TableSchema.fromBean(entityClass));
  }
}
