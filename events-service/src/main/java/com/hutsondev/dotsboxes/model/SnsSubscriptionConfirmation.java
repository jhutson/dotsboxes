package com.hutsondev.dotsboxes.model;

public record SnsSubscriptionConfirmation(
    String type,
    String token,
    String topicArn,
    String message,
    String subscribeURL,
    String timestamp,
    String signatureVersion,
    String signature,
    String signingCertURL
) {

}
