package com.meinc.ergo.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
//import org.codehaus.jackson.JsonNode;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.annotate.JsonIgnore;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.meinc.ergo.exception.InvalidParamException;
import com.meinc.ergo.exception.MissingRequiredParamException;
import com.meinc.ergo.util.Encryptor;
import com.meinc.ergo.util.JsonDateSerializer;

public class Provider
implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;

    public static final Logger logger = Logger.getLogger(Provider.class);
    private static ObjectMapper jsonMapper = new ObjectMapper();

    public static enum TYPE {ERGO, GOOGLE, EXCHANGE, APPLE}

    /** Provider properties **/

    @JsonProperty("providerId")
    private String providerUuid;
    private String displayName;
    private String userName;
    private String email;
    private String domain;
    private String server;
    private String webServer;
    private TYPE type;
    private Date createDate;
    private Date lastUpdate;
    private Date accessDate;
    private String syncStateTasks;
    private String syncStateNotes;
    private int resetSync;

    public String getProviderUuid()
    {
        return providerUuid;
    }
    public void setProviderUuid(String providerUuid)
    {
        this.providerUuid = providerUuid;
    }
    public String getDisplayName()
    {
        return displayName;
    }
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getServer()
    {
        return server;
    }
    public void setServer(String server)
    {
        this.server = server;
    }
    @JsonIgnore
    public String getWebServer() {
        return webServer;
    }
    public void setWebServer(String webServer) {
        this.webServer = webServer;
    }
    public TYPE getType()
    {
        return type;
    }
    public void setType(TYPE type)
    {
        this.type = type;
    }

    @JsonIgnore
    public String getSyncStateTasks() {
        return syncStateTasks;
    }
    public void setSyncStateTasks(String providerSyncStateTasks) {
        this.syncStateTasks = providerSyncStateTasks;
    }

    @JsonIgnore
    public String getSyncStateNotes() {
        return syncStateNotes;
    }
    public void setSyncStateNotes(String providerSyncStateNotes) {
        this.syncStateNotes = providerSyncStateNotes;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getCreateDate()
    {
        return createDate;
    }
    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getLastUpdate()
    {
        return lastUpdate;
    }
    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getAccessDate() {
        return accessDate;
    }

    public void setAccessDate(Date accessDate) {
        this.accessDate = accessDate;
    }

    @Override
    public String toString()
    {
        try {
            return jsonMapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.error("Could not serialize Provider to JSON: " + e.getMessage(), e);
            return String.format("{'providerId': '%s'}".replaceAll("'", "\""), providerUuid);
        }
    }

    /** ProviderCredential properties **/

    public static enum SSL_TYPE {ALWAYS, NEVER}

    private SSL_TYPE ssl;
    private String password;
    private String oAuthAuthToken;
    private boolean isEncrypted = false;
    private String authMethod;
    private Boolean fastConnect;
    private String serverVersion;

    /**
     * Call this to encrypt the sensitive values (for example, before sending this to the database)
     */
    public void encrypt(Encryptor encryptor)
    {
        if (isEncrypted) return;

        if (oAuthAuthToken != null) {
            oAuthAuthToken = encryptor.encrypt(oAuthAuthToken);
        }
        if (password != null) {
            password = encryptor.encrypt(password);
        }

        isEncrypted = true;
    }

    /**
     * Call this to decrypt the sensitive values (for example, after retrieving this from the database)
     */
    public void decrypt(Encryptor encryptor)
    {
        if (!isEncrypted) return;

        if (oAuthAuthToken != null) {
            oAuthAuthToken = encryptor.decrypt(oAuthAuthToken);
        }
        if (password != null) {
            password = encryptor.decrypt(password);
        }

        isEncrypted = false;
    }

    public static Provider fromJson(String raw, boolean required)
    throws InvalidParamException, MissingRequiredParamException
    {
        if (raw == null) {
            if (required)
                throw new MissingRequiredParamException();
            else
                return null;
        }

        try {
            Provider p = new Provider();
            JsonNode node = new ObjectMapper().readValue(raw, JsonNode.class);

            p.type = Provider.TYPE.valueOf(node.get("providerId").textValue());
            p.displayName = node.get("displayName").textValue();

            JsonNode payload = node.get("payload");
            switch (p.type)
            {
                case GOOGLE:
                    //userName is optional on Google (it's not really used for anything)
                    if (node.has("userName")) {
                        p.userName = node.get("userName").textValue().trim();
                    } else
                        p.userName = "N/A";
                    p.email = "N/A";
                    if (!payload.has("oauthAccessToken"))
                        throw new MissingRequiredParamException("oauthAccessToken");
                    p.oAuthAuthToken = payload.get("oauthAccessToken").textValue();
                    break;

                case ERGO:
                    p.userName = node.get("userName").textValue().trim();
                    p.password = payload.get("password").textValue();
                    p.email = "N/A";
                    break;

                case EXCHANGE:
                    if (!node.has("email") || node.get("email").isNull())
                        throw new MissingRequiredParamException("email");
                    p.email = node.get("email").textValue();

                    if (node.has("userName") && !node.get("userName").isNull()) {
                        p.userName = node.get("userName").textValue().trim();
                        p.userName = p.userName.isEmpty() ? null : p.userName;
                    }

                    p.password = payload.get("password").textValue();
                    if (payload.has("isEncrypted") && !payload.get("isEncrypted").isNull()) {
                        p.isEncrypted = payload.get("isEncrypted").booleanValue();
                    }

                    if (payload.has("domain") && !payload.get("domain").isNull()) {
                        p.domain = payload.get("domain").textValue().trim();
                        p.domain = p.domain.isEmpty() ? null : p.domain;
                    }

                    if (payload.has("server") && !payload.get("server").isNull()) {
                        p.server = payload.get("server").textValue().trim();
                        p.server = p.server.isEmpty() ? null : p.server;
                    }

                    p.ssl = SSL_TYPE.valueOf(payload.get("ssl").textValue());
                    break;
            }

            return p;

        } catch (JsonParseException e) {
            logger.error("unable to parse provider credential", e);
            throw new InvalidParamException();
        } catch (JsonMappingException e) {
            logger.error("unable to parse provider credential", e);
            throw new InvalidParamException();
        } catch (IOException e) {
            logger.error("unable to parse provider credential", e);
            throw new InvalidParamException();
        } catch (IllegalArgumentException e) {
            logger.error("unable to parse provider credential", e);
            throw new InvalidParamException();
        } catch (NullPointerException e) {
            logger.error("unable to parse provider credential", e);
            throw new InvalidParamException();
        }
    }

    @JsonIgnore
    public SSL_TYPE getSsl() {
        return ssl;
    }
    public void setSsl(SSL_TYPE ssl) {
        this.ssl = ssl;
    }
    @JsonIgnore
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @JsonIgnore
    public String getoAuthAuthToken() {
        return oAuthAuthToken;
    }
    public void setoAuthAuthToken(String oAuthAuthToken) {
        this.oAuthAuthToken = oAuthAuthToken;
    }
    @JsonIgnore
    public boolean isEncrypted() {
        return isEncrypted;
    }
    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }
    @JsonIgnore
    public String getAuthMethod() {
        return authMethod;
    }
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }
    @JsonIgnore
    public Boolean isFastConnect() {
        return fastConnect;
    }
    public void setFastConnect(Boolean fastConnect) {
        this.fastConnect = fastConnect;
    }
    @JsonIgnore
    public String getServerVersion() {
        return serverVersion;
    }
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public static enum Style {
        EXACT,
        SIMPLE,
        COMPLEX
    }

    @JsonIgnore
    public Style getStyle() {
        if (authMethod != null)
            return Style.EXACT;
        if (userName != null || domain != null || server != null)
            return Style.COMPLEX;
        return Style.SIMPLE;
    }
    public void reduceStyle(Style targetStyle) {
        switch (targetStyle) {
        case EXACT:
            throw new IllegalArgumentException("cannot reduce provider style to exact");
        case SIMPLE:
            userName = null;
            server = null;
            domain = null;
        case COMPLEX:
            authMethod = null;
            break;
        }
    }

    @Override
    public Provider clone() {
        try {
            return (Provider) super.clone();
        } catch (CloneNotSupportedException e) {
            String msg = "Could not clone Provider object: " + e.getMessage();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }
    public int getResetSync() {
        return resetSync;
    }
    public void setResetSync(int resetSync) {
        this.resetSync = resetSync;
    }
}
