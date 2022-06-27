package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A data structure to hold product rates.
 * 
 * @author bxgrant
 */
public class ProductRate implements Serializable
{
  private static final long serialVersionUID = -2698257303747739565L;
  
  public static final String RATE_SUBSCRIBER = "Subscriber";
  public static final String RATE_ACCOUNT = "Account"; 
  private static List<ProductRate> _rates;
  
  
  private int _rateId;
  private String _value;
  private String _description;
  private int _sortOrder;
  
  public ProductRate()
  {
  }
  
  public ProductRate(int rateId, String value, String description, int sortOrder)
  {
    _rateId = rateId;
    _value = value;
    _description = description;
    _sortOrder = sortOrder;
  }

  public static List<ProductRate> getRates()
  {
    ProductRate rate;
    
    if (_rates == null)
    {
      _rates = new ArrayList<ProductRate>();
      
      rate = new ProductRate(1, RATE_SUBSCRIBER, "Product price applied for " + 
                             "each subscriber per billing period", 1);
      _rates.add(rate);
      
      rate = new ProductRate(2, RATE_SUBSCRIBER, "Product price applied once " + 
                             "for the entire account for the billing period", 2);
      _rates.add(rate);                  
    }
    
    return _rates;
  }
  
  public static ProductRate getRate(int rateId)
  {
    ProductRate result = null;
    
    for (ProductRate rate : getRates())
    {
      if (rate.getRateId() == rateId)
      {
        result = rate;
        break;
      }
    }
    
    return result;
  }
  
  public boolean equals(ProductRate rate)
  {   
    return rate == null ? false : this.getRateId() == rate.getRateId();
  }
  
  /**
   * @return Returns the rateId.
   */
  public int getRateId()
  {
    return _rateId;
  }

  /**
   * @param rateId The rateId to set.
   */
  public void setRateId(int rateId)
  {
    _rateId = rateId;
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
