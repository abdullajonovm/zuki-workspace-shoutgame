package com.meinc.urlshorten.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

@DynamoDBTable(tableName="ShortenedUrl")
public class ShortenedUrl
{
    private String shortUrlOrigin;
    private String shortUrlCode;
    private String longUrl;
    private String createDate;
    private Long version;
    
    public ShortenedUrl() { }

    public ShortenedUrl(ShortenedUrl shortUrl) {
        shortUrlOrigin = shortUrl.shortUrlOrigin;
        shortUrlCode   = shortUrl.shortUrlCode;
        longUrl        = shortUrl.longUrl;
        createDate     = shortUrl.createDate;
        version        = shortUrl.version;
    }

    @DynamoDBAttribute(attributeName="ShortUrlOrigin")
    public String getShortUrlOrigin() {
        return shortUrlOrigin;
    }

    public void setShortUrlOrigin(String shortUrlOrigin) {
        this.shortUrlOrigin = shortUrlOrigin;
    }

    @DynamoDBHashKey(attributeName="ShortUrlCode")
    public String getShortUrlCode() {
        return shortUrlCode;
    }

    public void setShortUrlCode(String shortUrlCode)
    {
        this.shortUrlCode = shortUrlCode;
    }
    
    @DynamoDBAttribute(attributeName="LongUrl")
    @DynamoDBIndexHashKey(globalSecondaryIndexName="LongUrl")
    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
    
	private static final SimpleDateFormat iso8601Formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @DynamoDBAttribute(attributeName="CreateDate")
    public String getCreateDate()
    {
        if (createDate == null)
            return iso8601Formatter.format(new Date());
        return createDate;
    }
    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }
    
	@DynamoDBIgnore
    public Date getCreateDateAsDate() throws ParseException {
        if (createDate == null)
            return null;
        return iso8601Formatter.parse(createDate);
    }

    public void setCreateDate(Date createDate) {
        this.createDate = iso8601Formatter.format(createDate);
    }
    
    @DynamoDBVersionAttribute
    public Long getVersion()
    {
        if (version == null)
            return 1L;
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return  "{\"ShortUrlOrigin\":" + (shortUrlOrigin == null ? null : ("\""+shortUrlOrigin+"\"")) +
               ", \"ShortUrlCode\":"   + (shortUrlCode == null   ? null : ("\""+shortUrlCode+"\"")) +
               ", \"LongUrl\":"        + (longUrl == null        ? null : ("\""+longUrl+"\"")) +
               ", \"CreateDate\":"     + (createDate == null     ? null : ("\""+createDate+"\"")) +
               ", \"version\":"        + version + "}";
    }
}
