package com.meinc.zztasks.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.zztasks.provider.exchange.ExchangeColorMapper;
import com.meinc.zztasks.provider.exchange.ExchangeColorMapper.ErgoColor;
import com.meinc.zztasks.provider.exchange.ExchangeDataManager.EXCHANGE_VERSION;

public class Role
extends BaseEntityObject
{
    private static final long serialVersionUID = 1L;
//    private static final Logger _logger = Logger.getLogger(Role.class);

    private String name;
    private String color;

    @JsonIgnore
    private String providerColor;

    private String icon;

//    @JsonIgnore
//    private int numExtraSpaces;

    @JsonProperty("roleId")
    @Override
    public String getUuid()
    {
        return uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        if (color != null && color.trim().length() == 0)
            color = null;
        if (color != null) {
            if (color.trim().toLowerCase().startsWith("0x"))
                color = color.substring(2, color.length());
            else if (color.trim().length() <= 2) {
                try {
                    int colorIndex = Integer.parseInt(color);
                    ErgoColor ergoColor = ExchangeColorMapper.ErgoColor.fromColor(colorIndex);
                    color = ergoColor.getHex();
                } catch (NumberFormatException e) { }
            }
        }
        this.color = color;
    }

    public String getProviderColor() {
        return providerColor;
    }

    public void setProviderColor(String providerColor) {
        this.providerColor = providerColor;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

//    public int getNumExtraSpaces()
//    {
//        return numExtraSpaces;
//    }
//
//    public void setNumExtraSpaces(int numExtraSpaces)
//    {
//        this.numExtraSpaces = numExtraSpaces;
//    }

    @JsonCreator
    public static Role fromString(String escapedJson)
    {
        Role w = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            w = mapper.readValue(escapedJson, Role.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return w;
    }

    @JsonIgnore
    public static Role getTestStaleRole()
    {
        Role r = new Role();
        r.setName("stale role");
        return r;
    }

    @JsonIgnore
    public static List<String> getFieldNamesThatDiffer(Role r1, Role r2)
    {
        List<String> fieldsThatDiffer = new ArrayList<String>();

        if (r1.getName() == null && r2.getName() == null)
            ;
        else {
            if (r1.getName() != null && r2.getName() == null)
                fieldsThatDiffer.add("name");
            else if (r1.getName() == null && r2.getName() != null)
                fieldsThatDiffer.add("name");
            else if (!r1.getName().equals(r2.getName()))
                fieldsThatDiffer.add("name");
        }

//color conflicts are ignored
//        if (r1.getColor() == null && r2.getColor() == null)
//            ;
//        else {
//            if (r1.getColor() != null && r2.getColor() == null)
//                fieldsThatDiffer.add("color");
//            else if (r1.getColor() == null && r2.getColor() != null)
//                fieldsThatDiffer.add("color");
//            else if (!r1.getColor().equals(r2.getColor()))
//                fieldsThatDiffer.add("color");
//        }

//icon conflicts are ignored
//        if (r1.getIcon() == null && r2.getIcon() == null)
//            ;
//        else {
//            if (r1.getIcon() != null && r2.getIcon() == null)
//                fieldsThatDiffer.add("icon");
//            else if (r1.getIcon() == null && r2.getIcon() != null)
//                fieldsThatDiffer.add("icon");
//            else if (!r1.getIcon().equals(r2.getIcon()))
//                fieldsThatDiffer.add("icon");
//        }

//order conflicts are ignored
//        if (r1.getOrder() != r2.getOrder())
//            fieldsThatDiffer.add("order");

        return fieldsThatDiffer;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("roleId: ").append(getId());
        buf.append(", uuid: ").append(getUuid());
        buf.append(", name: ").append(name);
        buf.append(", color: ").append(color);
        buf.append(", icon: ").append(icon);
        buf.append(super.toString());

        return buf.toString();
    }

    public static String scrubName(String name, EXCHANGE_VERSION version)
    {
        //return name.replaceAll("[^A-Za-z0-9 _]", "");
        name = name.replaceAll("[,;]", "");

        //exchange 2007 doesn't allow < or >
        if (version == EXCHANGE_VERSION.v2007) {
            name = name.replaceAll("<", "");
            name = name.replaceAll(">", "");
            name = name.replaceAll("\u00a0", " ");
        }

        return name;
    }

}
