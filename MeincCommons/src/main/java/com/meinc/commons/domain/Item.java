package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.Date;

public class Item implements Serializable
{ 
  private static final long serialVersionUID = -8089722240546130817L;
  
  private int _itemId;
  private String _name;
  private ItemType _itemType;
  private String _version;
  private String _shortDesc;
  private String _longDesc;
  private ImageByteArray _image;
  private ImageByteArray _thumbnail;
  private String _fileName;
  private ProductStatus _status;
  private Date _createDate;
  private Date _lastUpdate;
  
  public Item()
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
   * @return Returns the fileName.
   */
  public String getFileName()
  {
    return _fileName;
  }

  /**
   * @param fileName The fileName to set.
   */
  public void setFileName(String fileName)
  {
    _fileName = fileName;
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
   * @return Returns the itemId.
   */
  public int getItemId()
  {
    return _itemId;
  }

  /**
   * @param itemId The itemId to set.
   */
  public void setItemId(int itemId)
  {
    _itemId = itemId;
  }

  /**
   * @return Returns the itemType.
   */
  public ItemType getItemType()
  {
    return _itemType;
  }

  /**
   * @param itemType The itemType to set.
   */
  public void setItemType(ItemType itemType)
  {
    _itemType = itemType;
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
   * @return Returns the longDesc.
   */
  public String getLongDesc()
  {
    return _longDesc;
  }

  /**
   * @param longDesc The longDesc to set.
   */
  public void setLongDesc(String longDesc)
  {
    _longDesc = longDesc;
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

  /**
   * @return Returns the version.
   */
  public String getVersion()
  {
    return _version;
  }

  /**
   * @param version The version to set.
   */
  public void setVersion(String version)
  {
    _version = version;
  }

}
