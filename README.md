# rapid-dynamodb
Example lightweight DynamoDB integration that limits AWS Lambda cold starts for Java. It is pure-Java8 implementation without third-part libraries that increases fat JAR size and cold starts time.

It uses DynamoDB Low-Level API described on https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Operations_Amazon_DynamoDB.html


# Getting Started

## Example AWS Lambda

This example assumes that there is DynamoDB Table **Test** created with Primary Key **uuid** with String type. It uses JSR-374 JSONP as JSON parser (https://javaee.github.io/jsonp/) for better readibility during preparing JSON requests.

JSON request in the following format:
```javascript
{
  "uuid": "NEW_UUID"
}
```

Maven dependencies:
```xml
<dependencies>
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-core</artifactId>
            <version>1.2.0</version>
        </dependency>
        <dependency>
            <groupId>com.r6lab.aws</groupId>
            <artifactId>rapid-dynamodb</artifactId>
            <version>0.1.0</version>
        </dependency>
        <dependency>
            <groupId>javax.json</groupId>
            <artifactId>javax.json-api</artifactId>
            <version>1.1.2</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.json</artifactId>
            <version>1.1.2</version>
        </dependency>
    </dependencies>
```

Example AWS Lambda handler that accepts request with uuid and after successful PutItem operation performs Scan and return all items:
```java
public class RapidDynamoDBLambda implements RequestHandler<Map<String, String>, String> {

    public String handleRequest(Map<String, String> event, Context context) {
        RapidDynamoDBClient rapidDynamoDBClient = RapidDynamoDBClient.envAware();

        DynamoDBRequest putItemRequest = DynamoDBRequest.of(Action.PUT_ITEM, getItemJson(event.get("uuid")));
        DynamoDBResponse response = rapidDynamoDBClient.execute(putItemRequest);

        if (response.isSuccess()) {
            DynamoDBRequest scanRequest = DynamoDBRequest.of(Action.SCAN, scanJson());
            return rapidDynamoDBClient.execute(scanRequest).getPayload();
        } else {
            context.getLogger().log(response.getPayload());
            throw new IllegalStateException("Unable to put item into DynamoDB");
        }
    }

    private String getItemJson(String uuid) {
        JsonObject getItemJson = Json.createObjectBuilder()
                .add("TableName", "Test")
                .add("Item", Json.createObjectBuilder()
                        .add("uuid", Json.createObjectBuilder()
                                .add("S", uuid)))
                .build();
        return getItemJson.toString();
    }

    private String scanJson() {
        JsonObject getItemJson = Json.createObjectBuilder()
                .add("TableName", "Test")
                .add("ReturnConsumedCapacity", "TOTAL")
                .build();
        return getItemJson.toString();
    }
}
```
