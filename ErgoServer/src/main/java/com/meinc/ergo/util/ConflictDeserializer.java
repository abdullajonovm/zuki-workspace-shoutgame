package com.meinc.ergo.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.ergo.domain.Conflict;
import com.meinc.ergo.domain.Note;
import com.meinc.ergo.domain.Role;
import com.meinc.ergo.domain.Task;

public class ConflictDeserializer
extends JsonDeserializer<Conflict>
{
    @Override
    public Conflict deserialize(JsonParser parser, DeserializationContext context)
    throws IOException, JsonProcessingException
    {
        ObjectCodec oc = parser.getCodec();
        JsonNode node = oc.readTree(parser);
        ObjectMapper mapper = new ObjectMapper();

        Conflict c = new Conflict();

        String cType = node.get("conflictType").textValue();
        Conflict.CONFLICT_TYPE conflictType = Conflict.CONFLICT_TYPE.valueOf(cType);
        c.setConflictType(conflictType);

        String oType = node.get("objectType").textValue();
        Conflict.OBJECT_TYPE objectType = Conflict.OBJECT_TYPE.valueOf(oType);
        c.setObjectType(objectType);

        String rType = node.get("resolutionType").textValue();
        Conflict.RESOLUTION_TYPE resolutionType = Conflict.RESOLUTION_TYPE.valueOf(rType);
        c.setResolutionType(resolutionType);

        List<String> fieldsInConflict = new ArrayList<String>();
        JsonNode conflictFields = node.get("fieldsInConflict");
        if (conflictFields != null && conflictFields.isArray()) {
            Iterator<JsonNode> it = conflictFields.iterator();
            while (it.hasNext()) {
                JsonNode arrayNode = it.next();
                fieldsInConflict.add(arrayNode.textValue());
            }
        }
        c.setFieldsInConflict(fieldsInConflict);

        JsonNode clientObjJson = node.get("clientObject");
        if (clientObjJson != null) {
            switch (objectType)
            {
                case ROLE:
                    Role r = mapper.readValue(clientObjJson, Role.class);
                    c.setClientObject(r);
                    break;

                case NOTE:
                    Note n = mapper.readValue(clientObjJson, Note.class);
                    c.setClientObject(n);
                    break;

                case TASK:
                    Task t = mapper.readValue(clientObjJson, Task.class);
                    c.setClientObject(t);
                    break;
            }
        }

        JsonNode serverObjJson = node.get("serverObject");
        if (serverObjJson != null) {
            switch (objectType)
            {
                case ROLE:
                    Role r = mapper.readValue(serverObjJson, Role.class);
                    c.setServerObject(r);
                    break;

                case NOTE:
                    Note n = mapper.readValue(serverObjJson, Note.class);
                    c.setServerObject(n);
                    break;

                case TASK:
                    Task t = mapper.readValue(serverObjJson, Task.class);
                    c.setServerObject(t);
                    break;
            }
        }

        return c;
    }

}
