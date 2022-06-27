package com.meinc.ergo.domain;


public class ApplicationInformation
{
    private String applicationId;
    private String applicationVersion;
    
    public String getApplicationId()
    {
        return applicationId;
    }
    public void setApplicationId(String applicationId)
    {
        this.applicationId = applicationId;
    }
    public String getApplicationVersion()
    {
        return applicationVersion;
    }
    public void setApplicationVersion(String applicationVersion)
    {
        this.applicationVersion = applicationVersion;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("applicationId: ").append(applicationId);
        buf.append(", applicationVersion: ").append(applicationVersion);

        return buf.toString();
    }
}
