package com.meinc.ergo.domain;

public class ExchangeConnectionInfo
{
    private String email;
    private String username;
    private String password;
    private String server;
    private String domain;
    
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public String getServer()
    {
        return server;
    }
    public void setServer(String server)
    {
        this.server = server;
    }
    public String getDomain()
    {
        return domain;
    }
    public void setDomain(String domain)
    {
        this.domain = domain;
    }
}
