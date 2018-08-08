package pl.r6lab.aws.dynamodb;

public class DynamoDBResponse {

    private final boolean success;
    private final String payload;

    private DynamoDBResponse(boolean success, String payload) {
        this.success = success;
        this.payload = payload;
    }

    public static DynamoDBResponse success(String response) {
        return new DynamoDBResponse(true, response);
    }

    public static DynamoDBResponse fail(String response) {
        return new DynamoDBResponse(false, response);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPayload() {
        return payload;
    }

}
