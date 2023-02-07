package com.hutsondev.dynamodb.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DynamoDbExtension implements BeforeAllCallback, AfterAllCallback {



  @Override
  public void afterAll(ExtensionContext context) throws Exception {

  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {

  }
}
