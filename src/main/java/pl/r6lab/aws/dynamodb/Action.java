package pl.r6lab.aws.dynamodb;

public enum Action {
    BATCH_GET_ITEM("BatchGetItem"),
    BATCH_WRITE_ITEM("BatchWriteItem"),
    CREATE_BACKUP("CreateBackup"),
    CREATE_GLOBAL_TABLE("CreateGlobalTable"),
    CREATE_TABLE("CreateTable"),
    DELETE_BACKUP("DeleteBackup"),
    DELETE_ITEM("DeleteItem"),
    DELETE_TABLE("DeleteTable"),
    DESCRIBE_BACKUP("DescribeBackup"),
    DESCRIBE_CONTINUOUS_BACKUPS("DescribeContinuousBackups"),
    DESCRIBE_GLOBAL_TABLE("DescribeGlobalTable"),
    DESCRIBE_GLOBAL_TABLE_SETTINGS("DescribeGlobalTableSettings"),
    DESCRIBE_LIMITS("DescribeLimits"),
    DESCRIBE_TABLE("DescribeTable"),
    DESCRIBE_TIME_TO_LIVE("DescribeTimeToLive"),
    GET_ITEM("GetItem"),
    LIST_BACKUPS("ListBackups"),
    LIST_GLOBAL_TABLES("ListGlobalTables"),
    LIST_TABLES("ListTables"),
    LIST_TAGS_OF_RESOURCE("ListTagsOfResource"),
    PUT_ITEM("PutItem"),
    QUERY("Query"),
    RESTORE_TABLE_FROM_BACKUP("RestoreTableFromBackup"),
    RESTORE_TABLE_TO_POINT_IN_TIME("RestoreTableToPointInTime"),
    SCAN("Scan"),
    TAG_RESOURCE("TagResource"),
    UNTAG_RESOURCE("UntagResource"),
    UPDATE_CONTINUOUS_BACKUPS("UpdateContinuousBackups"),
    UPDATE_GLOBAL_TABLE("UpdateGlobalTable"),
    UPDATE_GLOBAL_TABLE_SETTINGS("UpdateGlobalTableSettings"),
    UPDATE_ITEM("UpdateItem"),
    UPDATE_TABLE("UpdateTable"),
    UPDATE_TIME_TO_LIVE("UpdateTimeToLive");

    private final String name;

    Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
