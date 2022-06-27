package com.meinc.urlshorten.dao;

import java.util.Iterator;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.meinc.urlshorten.domain.ShortenedUrl;
import com.meinc.urlshorten.exception.ShortUrlAlreadyExistsException;
import com.meinc.urlshorten.exception.UrlShortenerException;

public class UrlShortenerDao
{
    //private static Logger _logger = LoggerFactory.getLogger(GroupDao.class);
    
    private String _serviceTableName;

    private AmazonDynamoDBClient _dynamoDbClient;
    private DynamoDB _dynamoDb;
    private DynamoDBMapper _dynamoDbMapper;

    @SuppressWarnings("unused")
    private static void createDynamoDbTable(String tableName, long readsPerSecondPerIndex, long writesPerSecondPerIndex, String dynamoDbAccessKey, String dynamoDbSecretKey) {
        UrlShortenerDao dao = new UrlShortenerDao(tableName, dynamoDbAccessKey, dynamoDbSecretKey);

        CreateTableRequest createTableRequest = dao._dynamoDbMapper.generateCreateTableRequest(ShortenedUrl.class);
        createTableRequest.setTableName(tableName);
        createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(readsPerSecondPerIndex, writesPerSecondPerIndex));

        List<GlobalSecondaryIndex> globalIndices = createTableRequest.getGlobalSecondaryIndexes();
        for (GlobalSecondaryIndex globalIndex : globalIndices) {
            globalIndex.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            globalIndex.getProjection().setProjectionType(ProjectionType.ALL);
        }

        dao._dynamoDbClient.createTable(createTableRequest);
    }

    public UrlShortenerDao(String serviceTableName, String dynamoDbAccessKey, String dynamoDbSecretKey)
    {
        _serviceTableName = serviceTableName;
        
        _dynamoDbClient = 
                        new AmazonDynamoDBClient(
                                new StaticCredentialsProvider(
                                        new BasicAWSCredentials(dynamoDbAccessKey, dynamoDbSecretKey)));
        _dynamoDb = new DynamoDB(_dynamoDbClient);
        DynamoDBMapperConfig dynamoDbMapperConfig = new DynamoDBMapperConfig(new TableNameOverride(serviceTableName));
        _dynamoDbMapper = new DynamoDBMapper(_dynamoDbClient, dynamoDbMapperConfig);
    }

    public void createShortUrl(ShortenedUrl shortenedUrl) throws ShortUrlAlreadyExistsException, UrlShortenerException {
        //TODO: use the DynamoDB Mapper API when it supports ConditionExpression
        Table relationsTable;
        try {
            relationsTable = _dynamoDb.getTable(_serviceTableName);
        } catch (AmazonClientException e) {
            throw new UrlShortenerException("error opening table " + _serviceTableName, e);
        }
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem( new Item()
                        .withPrimaryKey("ShortUrlCode", shortenedUrl.getShortUrlCode())
                        .withString("ShortUrlOrigin", shortenedUrl.getShortUrlOrigin())
                        .withString("LongUrl", shortenedUrl.getLongUrl())
                        .withString("CreateDate", shortenedUrl.getCreateDate())
                        .withNumber("version", shortenedUrl.getVersion()) )
                .withConditionExpression("attribute_not_exists(ShortUrlCode)");
                        
        try {
            relationsTable.putItem(putItemSpec);
        } catch (ConditionalCheckFailedException e) {
            throw new ShortUrlAlreadyExistsException(shortenedUrl.toString(), e);
        } catch (AmazonClientException e) {
            throw new UrlShortenerException(shortenedUrl.toString(), e);
        }
    }

    public ShortenedUrl getShortenedUrl(String shortUrl) throws UrlShortenerException {
        Table relationsTable;
        try {
            relationsTable = _dynamoDb.getTable(_serviceTableName);
        } catch (AmazonClientException e) {
            throw new UrlShortenerException("error opening table " + _serviceTableName, e);
        }
        
        ItemCollection<QueryOutcome> queryResults = relationsTable.query("ShortUrlCode", shortUrl);
        Iterator<Item> qoIt = queryResults.firstPage().iterator();
        if (!qoIt.hasNext())
            return null;

        ShortenedUrl result = new ShortenedUrl();

        while (qoIt.hasNext()) {
            Item item = qoIt.next();
            result.setShortUrlCode(item.getString("ShortUrlCode"));
            result.setShortUrlOrigin(item.getString("ShortUrlOrigin"));
            result.setLongUrl(item.getString("LongUrl"));
            result.setCreateDate(item.getString("CreateDate"));
            result.setVersion(item.getLong("version"));
        }
        
        return result;
    }

    public static void main(String[] args) {
        createDynamoDbTable("Prod_ShortenedUrls", 1, 1, "AKIAJW7DQ36VHSZ2M2LA", "R/aqHkj7gKnfuopKnTIFG85iqjOXIDvBs/+7EUPO");
    }
}
