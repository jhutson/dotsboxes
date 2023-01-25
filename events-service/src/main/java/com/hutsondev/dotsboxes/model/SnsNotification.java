package com.hutsondev.dotsboxes.model;

public record SnsNotification(
    String type,
    String messageId,
    String topicArn,
    String subject,
    String message,
    String timestamp,
    String signatureVersion,
    String signature,
    String signingCertURL,
    String unsubscribeURL
) {

}
