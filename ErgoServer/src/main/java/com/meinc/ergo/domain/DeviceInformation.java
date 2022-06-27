package com.meinc.ergo.domain;

public class DeviceInformation
{
    private String deviceId;
    private String model;
    private String name;
    private String osName;
    private String osType;
    private String version;
    
    public String getDeviceId()
    {
        return deviceId;
    }
    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }
    public String getModel()
    {
        return model;
    }
    public void setModel(String model)
    {
        this.model = model;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getOsName()
    {
        return osName;
    }
    public void setOsName(String osName)
    {
        this.osName = osName;
    }
    public String getOsType()
    {
        return osType;
    }
    public void setOsType(String osType)
    {
        this.osType = osType;
    }
    public String getVersion()
    {
        return version;
    }
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("deviceId: ").append(deviceId);
        buf.append(", model: ").append(model);
        buf.append(", name: ").append(name);
        buf.append(", osName: ").append(osName);
        buf.append(", osType: ").append(osType);
        buf.append(", version: ").append(version);

        return buf.toString();
    }
}
