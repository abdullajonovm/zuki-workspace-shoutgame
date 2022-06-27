package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A data structure to hold product rates.
 * 
 * @author bxgrant
 */
public class ItemType implements Serializable
{
  private static final long serialVersionUID = -6685007213119259666L;
  
  public static final String ITEM_TYPE_APPLICATION = "Application";
  public static final String ITEM_TYPE_SERVICE = "Service"; 
  private static List<ItemType> _itemTypes;  
  
  private int _itemTypeId;
  private String _value;
  private String _description;
  private int _sortOrder;
  
  public ItemType()
  {
  }
  
  public ItemType(int itemTypeId, String value, String description, int sortOrder)
  {
    _itemTypeId = itemTypeId;
    _value = value;
    _description = description;
    _sortOrder = sortOrder;
  }

  public static List<ItemType> getItemTypes()
  {
    ItemType itemType;
    
    if (_itemTypes == null)
    {
      _itemTypes = new ArrayList<ItemType>();
      
      itemType = new ItemType(1, ITEM_TYPE_APPLICATION, "Product price applied for " + 
                             "each subscriber per billing period", 1);
      _itemTypes.add(itemType);
      
      itemType = new ItemType(2, ITEM_TYPE_SERVICE, "Product price applied once " + 
                             "for the entire account for the billing period", 2);
      _itemTypes.add(itemType);                  
    }
    
    return _itemTypes;
  }
  
  public static ItemType getItemType(int itemTypeId)
  {
    ItemType result = null;
     
    for (ItemType itemType : getItemTypes())
    {
      if (itemType.getItemTypeId() == itemTypeId)
      {
        result = itemType;
        break;
      }
    }
    
    return result;
  }
  
  public boolean equals(ItemType itemType)
  {   
    return itemType == null ? false : this.getItemTypeId() == itemType.getItemTypeId();
  }
  
  /**
   * @return Returns the itemTypeId.
   */
  public int getItemTypeId()
  {
    return _itemTypeId;
  }

  /**
   * @param itemTypeId The itemTypeId to set.
   */
  public void setItemTypeId(int itemTypeId)
  {
    _itemTypeId = itemTypeId;
  }

  /**
   * @return Returns the description.
   */
  public String getDescription()
  {
    return _description;
  }

  /**
   * @param description The description to set.
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * @return Returns the sortOrder.
   */
  public int getSortOrder()
  {
    return _sortOrder;
  }

  /**
   * @param sortOrder The sortOrder to set.
   */
  public void setSortOrder(int sortOrder)
  {
    _sortOrder = sortOrder;
  }

  /**
   * @return Returns the value.
   */
  public String getValue()
  {
    return _value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(String value)
  {
    _value = value;
  }

}
