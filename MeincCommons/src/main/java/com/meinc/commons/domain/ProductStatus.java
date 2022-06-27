package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductStatus implements Serializable
{
  private static final long serialVersionUID = 4002071914255106674L;
  
  public static final String STATUS_ACTIVE = "Active";
  public static final String STATUS_INACTIVE = "Inactive"; 
  public static final String STATUS_SUSPENDED = "Suspended";  
  private static List<ProductStatus> _statuses;
  
  
  private int _statusId;
  private String _value;
  private String _description;
  private int _sortOrder;
  
  public ProductStatus()
  {
  }
  
  public ProductStatus(int statusId, String value, 
                       String description, int sortOrder)
  {
    _statusId = statusId;
    _value = value;
    _description = description;
    _sortOrder = sortOrder;
  }
  
  public static List<ProductStatus> getStatuses()
  {
    ProductStatus status;
    
    if (_statuses == null)
    {
      _statuses = new ArrayList<ProductStatus>();
      
      status = new ProductStatus(1, STATUS_ACTIVE, "Item is active", 1);
      _statuses.add(status);
      
      status = new ProductStatus(2, STATUS_INACTIVE, "Item is inactive", 2);
      _statuses.add(status);
      
      status = new ProductStatus(3, STATUS_SUSPENDED, "Item has been suspended", 3);
      _statuses.add(status);
    }
    
    return _statuses;
  }

  public static ProductStatus getStatus(int statusId)
  {
    ProductStatus result = null;
    
    for (ProductStatus status : getStatuses())
    {
      if (status.getStatusId() == statusId)
      {
        result = status;
        break;
      }
    }
    
    return result;
  }  
  
  public boolean equals(ProductStatus status)
  {
    return status == null ? false : this.getStatusId() == status.getStatusId();
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
   * @return Returns the statusId.
   */
  public int getStatusId()
  {
    return _statusId;
  }

  /**
   * @param statusId The statusId to set.
   */
  public void setStatusId(int statusId)
  {
    _statusId = statusId;
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
