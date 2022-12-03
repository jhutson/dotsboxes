package com.hutsondev.dotsboxes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

// Temporarily disable R2DBC configuration until data/repository classes are written.
@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
public class DotsboxesApplication {

  @Bean
  ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
  }

  public static void main(String[] args) {
    SpringApplication.run(DotsboxesApplication.class, args);
  }

}
