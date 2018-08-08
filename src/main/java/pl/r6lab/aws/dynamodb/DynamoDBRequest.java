package pl.r6lab.aws.dynamodb;

public class DynamoDBRequest {

    private final Action action;
    private final String payload;

    private DynamoDBRequest(Action action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public Action getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

    public static final DynamoDBRequest of(Action action, String payload) {
        return new DynamoDBRequest(action, payload);
    }
}
