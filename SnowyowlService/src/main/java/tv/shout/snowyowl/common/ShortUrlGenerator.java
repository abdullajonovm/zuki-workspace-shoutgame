package tv.shout.snowyowl.common;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.meinc.urlshorten.exception.ShortUrlAlreadyExistsException;
import com.meinc.urlshorten.exception.UrlShortenerException;
import com.meinc.urlshorten.service.IUrlShortenerService;

import tv.shout.util.JsonUtil;

public interface ShortUrlGenerator
{
    default String getShortUrl(IUrlShortenerService urlShortenerService, String shortUrlDomain, String shortUrlPrefix, String longUrl)
    throws IOException, UrlShortenerException
    {
        String shortUrl;
        try {
            shortUrl = urlShortenerService.makeShortUrl(shortUrlDomain, longUrl);
        } catch (ShortUrlAlreadyExistsException e) {
            //just return it.
            //ick, the message of the exception is a json representation of the ShortenedUrl object AND it doesn't contain the host
            JsonNode json = JsonUtil.getObjectMapper().readTree(e.getMessage());

            if (!shortUrlPrefix.endsWith("/")) shortUrlPrefix = shortUrlPrefix + "/";
            shortUrl = shortUrlPrefix + json.get("ShortUrlCode").asText();
        }

        return shortUrl;
    }
}
