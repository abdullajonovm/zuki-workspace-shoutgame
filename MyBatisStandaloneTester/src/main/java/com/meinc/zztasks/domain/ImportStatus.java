package com.meinc.zztasks.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class ImportStatus
implements Serializable
{
    private static final long serialVersionUID = -1632233464342919415L;

    public static enum STATUS {COMPLETE, INPROGRESS, PARTIAL, FAIL, NOTSTARTED};

    private STATUS status;
//    private double percentComplete;
    private String message;
    private List<String> failedOpIds;
    private List<String> successfulOpIds;

    private int completeItems;
    private int totalItems;

//    public static ImportStatus getNotStartedImportStatus()
//    {
//        ImportStatus is = new ImportStatus();
//        is.setStatus(STATUS.NOTSTARTED);
//        is.percentComplete = 0d;
//        is.message = null;
//        return is;
//    }

    public STATUS getStatus()
    {
        return status;
    }
    public void setStatus(STATUS status)
    {
        this.status = status;
    }
    public double getPercentComplete()
    {
        //return percentComplete;
        if (totalItems == 0) return 0D;
        return (double) completeItems / totalItems;
    }
//    public void setPercentComplete(double percentComplete)
//    {
//        this.percentComplete = percentComplete;
//    }
    public String getMessage()
    {
        return message;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<String> getFailedOpIds()
    {
        return failedOpIds;
    }

    /*public void setFailedOpIds(List<String> failedOpIds)
    {
        this.failedOpIds = failedOpIds;
    }*/

    public List<String> getSuccessfulOpIds()
    {
        return successfulOpIds;
    }

    /*public void setSuccessfulOpIds(List<String> successfulOpIds)
    {
        this.successfulOpIds = successfulOpIds;
    }*/

    public void setCompleteItems(int completeItems)
    {
        this.completeItems = completeItems;
    }
    public void setTotalItems(int totalItems)
    {
        this.totalItems = totalItems;
    }

    public void setFailedOpIdsRaw(String failedOpIdsRaw)
    {
        if (failedOpIdsRaw != null) {
            String[] vals = failedOpIdsRaw.split(",");
            List<String> list = Arrays.asList(vals);
            failedOpIds = list;
        }
    }

    public void setSuccessfulOpIdsRaw(String successfulOpIdsRaw)
    {
        if (successfulOpIdsRaw != null) {
            String[] vals = successfulOpIdsRaw.split(",");
            List<String> list = Arrays.asList(vals);
            successfulOpIds = list;
        }
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "status: {0}, %: {1}, msg: {2}, failedOpIds: {3}, successfulOpIds: {4}, completeItems: {5}, totalItems: {6}",
            status, getPercentComplete(), message, failedOpIds, successfulOpIds, completeItems, totalItems);
    }
}
