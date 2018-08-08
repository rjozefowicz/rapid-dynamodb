package pl.r6lab.aws.dynamodb;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.isNull;

public final class RapidDynamoDBClient {

    private static final String AWS_ACCESS_KEY_ENV_VARIABLE = "AWS_ACCESS_KEY";
    private static final String AWS_SECRET_KEY_ENV_VARIABLE = "AWS_SECRET_KEY";
    private static final String AWS_SESSION_TOKEN_ENV_VARIABLE = "AWS_SESSION_TOKEN";
    private static final String AWS_REGION_ENV_VARIABLE = "AWS_REGION";

    private static final String SIGNATURE_KEY_DATE_PATTERN = "YYYYMMdd";
    private static final String AWS_DATE_PATTERN = "YYYYMMdd'T'HHmmss'Z'";
    private static final DateTimeFormatter SIGNATURE_KEY_DATE_FORMATTER = DateTimeFormatter.ofPattern(SIGNATURE_KEY_DATE_PATTERN);
    private static final DateTimeFormatter AWS_DATE_FORMATTER = DateTimeFormatter.ofPattern(AWS_DATE_PATTERN);

    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String SERVICE = "dynamodb";
    private static final String HTTP_METHOD = "POST";
    private static final String SIGNED_HEADERS = "content-length;content-type;host;x-amz-date;x-amz-target";
    private static final String CANONICAL_URI = "/";
    private static final String NEW_LINE = "\n";
    private static final String DOT = ".";
    private static final String API_VERSION = "DynamoDB_20120810";

    private final String accessKey;
    private final String secretKey;
    private final String sessionToken;
    private final String region;

    private RapidDynamoDBClient(String accessKey, String secretKey, String sessionToken, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.sessionToken = sessionToken;
        this.region = region;
    }

    public DynamoDBResponse execute(DynamoDBRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String signatureDate = now.toLocalDate().format(SIGNATURE_KEY_DATE_FORMATTER);
        String awsDate = now.format(AWS_DATE_FORMATTER);

        try {
            HttpURLConnection connection = initConnection();
            byte[] signingKey = SignatureVersion4.getSignatureKey(this.secretKey, now.toLocalDate(), this.region, SERVICE);
            int contentLength = request.getPayload().getBytes().length;
            setBasicHeaders(connection, request, awsDate, contentLength);

            // Used for Temporary Security Credentials
            if (this.sessionToken != null) {
                connection.setRequestProperty("x-amz-security-token", sessionToken);
            }

            String stringToSign = stringToSign(contentLength, awsDate, signatureDate, request.getPayload(), request.getAction());
            String signature = signature(signingKey, stringToSign);
            String authorizationHeader = authorizationHeader(signature, signatureDate);

            connection.setRequestProperty("Authorization", authorizationHeader);
            connection.getOutputStream().write(request.getPayload().getBytes());
            connection.getOutputStream().flush();
            return handleResponse(connection);
        } catch (Exception e) {
            throw new RapidDynamoDBClientException(e);
        }
    }

    public final static RapidDynamoDBClient envAware() {
        return new RapidDynamoDBClient(System.getenv(AWS_ACCESS_KEY_ENV_VARIABLE), System.getenv(AWS_SECRET_KEY_ENV_VARIABLE), System.getenv(AWS_SESSION_TOKEN_ENV_VARIABLE), System.getenv(AWS_REGION_ENV_VARIABLE));
    }

    public final static RapidDynamoDBClient of(String accessKey, String secretKey, String sessionToken, String region) {
        if (isNull(accessKey) || isNull(secretKey) || isNull(region)) {
            throw new IllegalArgumentException("Missing mandatory AWS parameters");
        }
        return new RapidDynamoDBClient(accessKey, secretKey, sessionToken, region);
    }

    private HttpURLConnection initConnection() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://" + host()).openConnection();
            connection.setRequestMethod(HTTP_METHOD);
            connection.setDoOutput(true);
            return connection;
        } catch (IOException e) {
            throw new RapidDynamoDBClientException(e);
        }
    }


    private DynamoDBResponse handleResponse(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() == 200) {
            return DynamoDBResponse.success(getResponse(connection.getInputStream()));
        } else {
            return DynamoDBResponse.fail(getResponse(connection.getErrorStream()));
        }
    }

    private void setBasicHeaders(HttpURLConnection connection, DynamoDBRequest request, String awsDate, int contentLength) {
        connection.setRequestProperty("Content-Length", Integer.toString(contentLength));
        connection.setRequestProperty("Content-Type", "application/x-amz-json-1.0");
        connection.setRequestProperty("Host", host());
        connection.setRequestProperty("X-Amz-Date", awsDate);
        connection.setRequestProperty("X-Amz-Target", API_VERSION + DOT + request.getAction().getName());
    }

    private String getResponse(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        return content.toString();
    }

    private String stringToSign(int contentLength, String awsDate, String signatureDate, String payload, Action action) throws NoSuchAlgorithmException {
        String canonicalRequest = hexBinary(SignatureVersion4.sha256(canonicalRequest(contentLength, awsDate, payload, action))).toLowerCase();
        String stringToSign = ALGORITHM + NEW_LINE + awsDate + NEW_LINE + credentialsScope(signatureDate) + NEW_LINE + canonicalRequest;
        return stringToSign;
    }

    private String authorizationHeader(String signature, String signatureDate) {
        return ALGORITHM + " Credential=" + this.accessKey + '/' + credentialsScope(signatureDate) + ", SignedHeaders=" + SIGNED_HEADERS + ", Signature=" + signature;
    }

    private String canonicalRequest(int contentLength, String awsDate, String payload, Action action) throws NoSuchAlgorithmException {
        String hashedPayload = hexBinary(SignatureVersion4.sha256(payload)).toLowerCase();
        String canonicalRequest = HTTP_METHOD + NEW_LINE
                + CANONICAL_URI + NEW_LINE + NEW_LINE
                + "content-length:" + contentLength + NEW_LINE
                + "content-type:application/x-amz-json-1.0" + NEW_LINE
                + canonicalHeaders(awsDate) + NEW_LINE
                + "x-amz-target:" + API_VERSION + DOT + action.getName() + NEW_LINE
                + NEW_LINE + SIGNED_HEADERS + NEW_LINE
                + hashedPayload;
        return canonicalRequest;
    }

    private String signature(byte[] signingKey, String stringToSign) throws Exception {
        return hexBinary(SignatureVersion4.hmacSHA256(stringToSign, signingKey)).toLowerCase();
    }

    private String hexBinary(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    private String canonicalHeaders(String awsDate) {
        return "host:" + host() + NEW_LINE + "x-amz-date:" + awsDate;
    }

    private String credentialsScope(String signatureDate) {
        return signatureDate + "/" + region + "/" + SERVICE + "/" + "aws4_request";
    }

    private String host() {
        return SERVICE + DOT + this.region + DOT + "amazonaws.com";
    }

}
