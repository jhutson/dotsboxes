package com.hutsondev.authservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SampleController {

  private static final Logger logger = LoggerFactory.getLogger(SampleController.class);

  @GetMapping(value = "/test")
  public String test() {
    return "This is a test.";
  }
}
