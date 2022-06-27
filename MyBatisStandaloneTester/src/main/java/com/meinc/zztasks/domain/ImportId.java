package com.meinc.zztasks.domain;

import java.text.MessageFormat;

/**
 * Contain's an Id (along with an optional newId). used by the import functions
 */
public class ImportId
{
    public String origRoleUuid;
    public String newRoleUuid;
    public int newRoleId;
    public String noteUuid;
    public String taskUuid;

    @Override
    public String toString()
    {
        return MessageFormat.format("orUID: {0}, nrUID: {1}, nRid: {2}, nUID: {3}, tUID: {4}",
                origRoleUuid, newRoleUuid, newRoleId, noteUuid, taskUuid);
    }
}
