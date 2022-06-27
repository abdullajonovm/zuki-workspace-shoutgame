package com.meinc.store.domain;

import java.io.Serializable;
import java.text.MessageFormat;

public class CreditCardInfo
implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String number;
    private String expDate;
    private String cvv;
    private String streetAddress;
    private String extAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
    private String amount;
    private boolean defaultCard; //true if this is set as the subscriber's default card

    /** visa, mastercard, etc. */
    private String cardType;

    /** if this card references some third party instance of a credit card, this contains that reference */
    private String externalRefId;


    /**
     * @return true if all required fields are present, otherwise false
     */
    public boolean isComplete() {
        return  name          != null && !name         .trim().isEmpty() &&
                number        != null && !number       .trim().isEmpty() &&
                expDate       != null && !expDate      .trim().isEmpty() &&
                cvv           != null && !cvv          .trim().isEmpty() &&
                //streetAddress != null && !streetAddress.trim().isEmpty() &&
                //locality      != null && !locality     .trim().isEmpty() &&
                postalCode    != null && !postalCode   .trim().isEmpty() &&
                country       != null && country.trim().length() == 2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getExtAddress() {
        return extAddress;
    }

    public void setExtAddress(String extAddress) {
        this.extAddress = extAddress;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isDefaultCard()
    {
        return defaultCard;
    }

    public void setDefaultCard(boolean defaultCard)
    {
        this.defaultCard = defaultCard;
    }

    public String getCardType()
    {
        return cardType;
    }

    public void setCardType(String cardType)
    {
        this.cardType = cardType;
    }

    public String getExternalRefId()
    {
        return externalRefId;
    }

    public void setExternalRefId(String externalRefId)
    {
        this.externalRefId = externalRefId;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "{0}, exp: {1}, last4: {2}, default: {3}, extRef: {4}",
            cardType, expDate, number, defaultCard, externalRefId);
    }
}
