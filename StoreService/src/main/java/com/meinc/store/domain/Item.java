package com.meinc.store.domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class Item implements Serializable
{

    private static final long serialVersionUID = -50932815915518252L;
    public static final int classId = 3000;

    public static enum DurationUnit {
        HOURS, DAYS, MONTHS, YEARS;

        public int toCalendarUnit() {
            switch (this) {
            case HOURS:
                return Calendar.HOUR;
            case DAYS:
                return Calendar.DAY_OF_YEAR;
            case MONTHS:
                return Calendar.MONTH;
            case YEARS:
                return Calendar.YEAR;
            default:
                throw new IllegalStateException("Unknown DurationUnit: " + this);
            }
        }
    }

    private int _itemId;
    private String _storeBundleId;
    private String _uuid;
    private String _title;
    private String _description;
    private String _price;
    private boolean _active;
    private Integer _durationQuantity;
    private DurationUnit _durationUnit;
    private List<ItemPrice> _itemPrice;

    public Item()
    {
    }

    public Item(
        int itemId, String itemUuid, String storeBundleId,
        String title, String description, String price, boolean active,
        Integer durationQuantity, DurationUnit durationUnit, List<ItemPrice> itemPrice)
    {
        _itemId = itemId;
        _uuid = itemUuid;
        _storeBundleId = storeBundleId;
        _title = title;
        _description = description;
        _price = price;
        _active = active;
        _durationQuantity = durationQuantity;
        _durationUnit = durationUnit;
        _itemPrice = itemPrice;
    }

    public int getItemId()
    {
        return _itemId;
    }

    public void setItemId(int itemId)
    {
        _itemId = itemId;
    }

    public String getStoreBundleId() {
        return _storeBundleId;
    }

    public void setStoreBundleId(String storeBundleId) {
        _storeBundleId = storeBundleId;
    }

    public String getUuid()
    {
        return _uuid;
    }

    public void setUuid(String uuid)
    {
        _uuid = uuid;
    }

    public String getTitle()
    {
        return _title;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public String getPrice()
    {
        return _price;
    }

    public void setPrice(String price)
    {
        _price = price;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public Integer getDurationQuantity() {
        return _durationQuantity;
    }

    public void setDurationQuantity(Integer durationQuantity) {
        _durationQuantity = durationQuantity;
    }

    public DurationUnit getDurationUnit() {
        return _durationUnit;
    }

    public void setDurationUnit(DurationUnit durationUnit) {
        _durationUnit = durationUnit;
    }

    public List<ItemPrice> getItemPrice()
    {
        return _itemPrice;
    }

    public void setItemPrice(List<ItemPrice> itemPrice)
    {
        _itemPrice = itemPrice;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{itemId: ").append(_itemId).append(", uuid: ").append(_uuid).append(", title: ").append(_title).append(", description: ").append(_description);
        sb.append(", price: ").append(_price);
        sb.append(", active: ").append(_active).append(", durationHours: ").append(_durationQuantity);

        sb.append(", itemPrice:");
        for (ItemPrice ip : _itemPrice) {
            sb.append("\n\t").append(ip);
        }
        sb.append("}");

        return sb.toString();
    }
 }
