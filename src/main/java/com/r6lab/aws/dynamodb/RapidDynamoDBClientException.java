package com.r6lab.aws.dynamodb;

public class RapidDynamoDBClientException extends RuntimeException {

    public RapidDynamoDBClientException(String message) {
        super(message);
    }

    public RapidDynamoDBClientException(Throwable cause) {
        super(cause);
    }
}
