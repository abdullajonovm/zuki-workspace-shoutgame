package com.meinc.commons.postoffice.service;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

public class Attachment
implements Serializable
{
    private static final long serialVersionUID = -1505789973407624005L;

    private String _filename;
    private DataSource _datasource;

    private Attachment()
    {

    }

    public static Attachment fromStringSource(String data, String filename)
    {
        Attachment att = new Attachment();

        DataSource ds = null;
        try {
            ds = new ByteArrayDataSource(data.getBytes("UTF-8"), "application/octet-stream");
        } catch (UnsupportedEncodingException ignored) {
            //will not happen; if utf-8 isn't supported, there are bigger problems
        }
        att._datasource = ds;
        att._filename = filename;

        return att;
    }

    public String getFilename()
    {
        return _filename;
    }

    public DataSource getDatasource()
    {
        return _datasource;
    }

}
