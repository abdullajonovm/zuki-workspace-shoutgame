package com.meinc.store.domain;

import java.io.Serializable;
import java.util.Date;

public class ReceiptItem implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int _receiptId;
    private String _receiptUuid;
    private String _itemUuid;
    private String _itemName;
    private double _itemPrice;
    private String _purchaseType;
    private Date _purchaseDate;
    private String _purchaseCurrency;
    private long _subscriberId;

    public int getReceiptId() {
        return _receiptId;
    }
    public void setReceiptId(int _receiptId) {
        this._receiptId = _receiptId;
    }
    public String getReceiptUuid() {
        return _receiptUuid;
    }
    public void setReceiptUuid(String _receiptUuid) {
        this._receiptUuid = _receiptUuid;
    }
    public String getItemUuid() {
        return _itemUuid;
    }
    public void setItemUuid(String _itemUuid) {
        this._itemUuid = _itemUuid;
    }
    public String getItemName() {
        return _itemName;
    }
    public void setItemName(String _itemName) {
        this._itemName = _itemName;
    }
    public double getItemPrice() {
        return _itemPrice;
    }
    public void setItemPrice(double _itemPrice) {
        this._itemPrice = _itemPrice;
    }
    public String getPurchaseType() {
        return _purchaseType;
    }
    public void setPurchaseType(String _purchaseType) {
        this._purchaseType = _purchaseType;
    }
    public Date getPurchaseDate() {
        return _purchaseDate;
    }
    public void setPurchaseDate(Date _purchaseDate) {
        this._purchaseDate = _purchaseDate;
    }
    public String getPurchaseCurrency() {
        return _purchaseCurrency;
    }
    public void setPurchaseCurrency(String _purchaseCurrency) {
        this._purchaseCurrency = _purchaseCurrency;
    }
    public long getSubscriberId() {
        return _subscriberId;
    }
    public void setSubscriberId(long value) {
        this._subscriberId = value;
    }
}
