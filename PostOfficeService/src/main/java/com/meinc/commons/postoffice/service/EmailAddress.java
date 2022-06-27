package com.meinc.commons.postoffice.service;

import java.io.Serializable;

public class EmailAddress implements Serializable
{
    private static final long serialVersionUID = 8997071060616329073L;

    public String address;
    public String name;

    public EmailAddress()
    {
    }

    public EmailAddress(String address, String name)
    {
        this.address = address;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return String.format("%s <%s>", address, (name == null ? " " : name));
    }

    public String toSerialString()
    {
        String a = (address == null ? "" : address);
        String n = (name == null ? "" : name);
        return String.format("%04d%s", a.length(), a) + String.format("%04d%s", n.length(), n);
    }

    public int fromSerialString(String serial, int offset)
    {
        String lengthString = serial.substring(offset, offset + 4);
        int length = Integer.parseInt(lengthString);
        offset += 4;

        if (length == 0)
            address = null;
        else
            address = serial.substring(offset, offset + length);
        offset += length;

        lengthString = serial.substring(offset, offset + 4);
        length = Integer.parseInt(lengthString);
        offset += 4;

        if (length == 0)
            name = null;
        else
            name = serial.substring(offset, offset + length);
        offset += length;

        return offset;
    }

}
