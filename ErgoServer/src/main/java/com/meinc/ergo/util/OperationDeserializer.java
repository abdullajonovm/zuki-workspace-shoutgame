package com.meinc.ergo.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.ergo.domain.Note;
import com.meinc.ergo.domain.Operation;
import com.meinc.ergo.domain.Operation.CASCADE_TYPE;
import com.meinc.ergo.domain.Operation.OBJECT_TYPE;
import com.meinc.ergo.domain.Operation.OPERATION_TYPE;
import com.meinc.ergo.domain.Role;
import com.meinc.ergo.domain.Task;

public class OperationDeserializer
extends JsonDeserializer<Operation>
{
    @Override
    public Operation deserialize(JsonParser parser, DeserializationContext context)
    throws IOException, JsonProcessingException
    {
        ObjectCodec oc = parser.getCodec();
        JsonNode node = oc.readTree(parser);
        ObjectMapper mapper = new ObjectMapper();

        Operation op = new Operation();

        String opType = node.get("operationType").textValue();
        Operation.OPERATION_TYPE operationType = OPERATION_TYPE.valueOf(opType);
        op.setOperationType(operationType);

        String objType = node.get("objectType").textValue();
        Operation.OBJECT_TYPE objectType = OBJECT_TYPE.valueOf(objType);
        op.setObjectType(objectType);

        op.setOperationId(node.get("operationId").textValue());

        JsonNode objectJson = node.get("object");
        switch (objectType)
        {
            case ROLE:
                if (operationType == OPERATION_TYPE.DELETE) {
                    String cascadeTypeStr = node.get("cascadeType").textValue();
                    Operation.CASCADE_TYPE cascadeType = CASCADE_TYPE.valueOf(cascadeTypeStr);
                    op.setCascadeType(cascadeType);
                }

                Role r = mapper.readValue(objectJson, Role.class);
                op.setObject(r);
                break;

            case NOTE:
                Note n = mapper.readValue(objectJson, Note.class);
                op.setObject(n);
                break;

            case TASK:
                Task t = mapper.readValue(objectJson, Task.class);
                op.setObject(t);
                break;
        }

        return op;
    }

}
