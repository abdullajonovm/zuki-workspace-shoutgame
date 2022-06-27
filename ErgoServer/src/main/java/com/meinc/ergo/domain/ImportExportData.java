package com.meinc.ergo.domain;

import java.util.List;

/**
 * Helper class to deserialize bulk export data from JSON to objects
 */
public class ImportExportData
{
    public String providerId;
    public String transactionId;
    public boolean clearDestinationDataBeforeImport; 
    public List<Role> roles;
    public List<Note> notes;
    public List<Task> tasks;
    public String batchId;
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("\nproviderId: ").append(providerId);
        buf.append("\ntransactionId: ").append(transactionId);
        buf.append("\nROLES:\n");
        for (Role r : roles)
            buf.append("\n\t").append(r);
        buf.append("\nNOTES:\n");
        for (Note n : notes)
            buf.append("\n\t").append(n);
        buf.append("\nTASKS:\n");
        for (Task t: tasks)
            buf.append("\n\t").append(t);

        return buf.toString();
    }
}
