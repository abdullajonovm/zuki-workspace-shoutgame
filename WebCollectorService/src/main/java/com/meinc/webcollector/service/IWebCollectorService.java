package com.meinc.webcollector.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.webcollector.message.CollectorMessage;

/**
 * @author mpontius
 */
public interface IWebCollectorService
{
    public static final String WEBCOLLECTOR_SERVICE = "WebCollectorService";
    public static final String WEBCOLLECTOR_INTERFACE = "IWebCollectorService";
    
    void onServiceStart() throws IOException;
    void onServiceStop();
    public String addMessageToBuffer(CollectorMessage message);
    
    public void registerMessageTypeHandler(CollectorEndpoint collectorPath, String messageType, ServiceEndpoint messageTypeHandlerEndpoint);
    public void unregisterMessageTypeHandler(String messageType);
    
    public static class CollectorEndpoint {
        public Map<String,String> caNameByRoleName = Collections.emptyMap();
        
        public static enum HandlerStyle {
            ASYNC_MESSAGE,
            SYNC_REQUEST
        }

        public static enum ConnectionType {
            /** Requires a SSL connection where the client has been authenticated using a trusted certificate */
            AUTH_SSL,
            /** Requires a SSL connection */
            SSL,
            /** Requires an "internal" connection. Messages must be created using service methods. */
            INTERNAL,
            /** Any connection is accepted */
            ANY
        }

        private String path;
        private boolean pathIsPrefix;
        private ConnectionType requiredConnectionType;
        private HandlerStyle handlerStyle;
        private String[] allowedRoles;

        /**
         * If no allowed roles are specified, then role is not enforced for this endpoint
         * @param path
         * @param requiredConnectionType
         * @param allowedRoles
         */
        public CollectorEndpoint(String path, ConnectionType requiredConnectionType, String...allowedRoles) {
            this(path, false, requiredConnectionType, allowedRoles);
        }

        public CollectorEndpoint(String path, boolean pathIsPrefix, ConnectionType requiredConnectionType, String...allowedRoles) {
            this(path, pathIsPrefix, requiredConnectionType, HandlerStyle.ASYNC_MESSAGE, allowedRoles);
        }

        public CollectorEndpoint(String path, ConnectionType requiredConnectionType, HandlerStyle handlerStyle, String...allowedRoles) {
            this(path, false, requiredConnectionType, handlerStyle, allowedRoles);
        }

        public CollectorEndpoint(String path, boolean pathIsPrefix, ConnectionType requiredConnectionType, HandlerStyle handlerStyle, String...allowedRoles) {
            this.path = path;
            this.pathIsPrefix = pathIsPrefix;
            this.handlerStyle = handlerStyle;
            this.requiredConnectionType = requiredConnectionType;
            if (allowedRoles.length > 0) {
                for (String allowedRole : allowedRoles)
                    if (!caNameByRoleName.containsKey(allowedRole))
                        throw new IllegalArgumentException("Unknown allowed role specified: " + allowedRole);
                this.allowedRoles = Arrays.copyOf(allowedRoles, allowedRoles.length);
            }
        }

        public String getPath() {
            return path;
        }

        public boolean isPathIsPrefix() {
            return pathIsPrefix;
        }

        public ConnectionType getType() {
            return requiredConnectionType;
        }

        public String[] getAllowedRoles() {
            return allowedRoles;
        }

        public HandlerStyle getHandlerStyle() {
            return handlerStyle;
        }
    }
}
