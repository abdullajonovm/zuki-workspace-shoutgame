package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Product implements Serializable
{
  private static final long serialVersionUID = -4425882962948220395L;
  
  private int _productId;
  private String _name;
  private String _shortDesc;
  private  ImageByteArray _image;
  private ImageByteArray _thumbnail;
  private double _price;
  private ProductRate _rate;
  private ProductStatus _status;
  private Date _createDate;
  private Date _lastUpdate;
  public List<Item> _items;
  
  public Product()
  {
  }

  /**
   * @return Returns the createDate.
   */
  public Date getCreateDate()
  {
    return _createDate;
  }

  /**
   * @param createDate The createDate to set.
   */
  public void setCreateDate(Date createDate)
  {
    _createDate = createDate;
  }

  /**
   * @return Returns the image.
   */
  public ImageByteArray getImage()
  {
    return _image;
  }

  /**
   * @param image The image to set.
   */
  public void setImage(ImageByteArray image)
  {
    _image = image;
  }

  /**
   * @return Returns the items.
   */
  public List<Item> getItems()
  {
    return _items;
  }

  /**
   * @param items The items to set.
   */
  public void setItems(List<Item> items)
  {
    _items = items;
  }

  /**
   * @return Returns the lastUpdate.
   */
  public Date getLastUpdate()
  {
    return _lastUpdate;
  }

  /**
   * @param lastUpdate The lastUpdate to set.
   */
  public void setLastUpdate(Date lastUpdate)
  {
    _lastUpdate = lastUpdate;
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return _name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name)
  {
    _name = name;
  }

  /**
   * @return Returns the price.
   */
  public double getPrice()
  {
    return _price;
  }

  /**
   * @param price The price to set.
   */
  public void setPrice(double price)
  {
    _price = price;
  }

  /**
   * @return Returns the productId.
   */
  public int getProductId()
  {
    return _productId;
  }

  /**
   * @param productId The productId to set.
   */
  public void setProductId(int productId)
  {
    _productId = productId;
  }

  /**
   * @return Returns the rate.
   */
  public ProductRate getRate()
  {
    return _rate;
  }

  /**
   * @param rate The rate to set.
   */
  public void setRate(ProductRate rate)
  {
    _rate = rate;
  }

  /**
   * @return Returns the shortDesc.
   */
  public String getShortDesc()
  {
    return _shortDesc;
  }

  /**
   * @param shortDesc The shortDesc to set.
   */
  public void setShortDesc(String shortDesc)
  {
    _shortDesc = shortDesc;
  }

  /**
   * @return Returns the status.
   */
  public ProductStatus getStatus()
  {
    return _status;
  }

  /**
   * @param status The status to set.
   */
  public void setStatus(ProductStatus status)
  {
    _status = status;
  }

  /**
   * @return Returns the thumbnail.
   */
  public ImageByteArray getThumbnail()
  {
    return _thumbnail;
  }

  /**
   * @param thumbnail The thumbnail to set.
   */
  public void setThumbnail(ImageByteArray thumbnail)
  {
    _thumbnail = thumbnail;
  }

}
