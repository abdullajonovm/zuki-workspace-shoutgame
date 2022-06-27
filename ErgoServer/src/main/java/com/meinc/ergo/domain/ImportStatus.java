package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.List;

public class ImportStatus
implements Serializable
{
    private static final long serialVersionUID = -1632233464342919415L;
    
    public static enum STATUS {COMPLETE, INPROGRESS, PARTIAL, FAIL, NOTSTARTED};

    private STATUS status;
    private double percentComplete;
    private String message;
    private List<String> failedOpIds;
    private List<String> successfulOpIds;
    
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
        return percentComplete;
    }
    public void setPercentComplete(double percentComplete)
    {
        this.percentComplete = percentComplete;
    }
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

    public void setFailedOpIds(List<String> failedOpIds)
    {
        this.failedOpIds = failedOpIds;
    }

    public List<String> getSuccessfulOpIds()
    {
        return successfulOpIds;
    }

    public void setSuccessfulOpIds(List<String> successfulOpIds)
    {
        this.successfulOpIds = successfulOpIds;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("status: ").append(status);
        buf.append(", %: ").append(percentComplete);
        buf.append(", msg: ").append(message);

        return buf.toString();
    }
}
