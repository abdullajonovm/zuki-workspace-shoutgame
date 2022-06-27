package com.meinc.store.domain;

public class DefaultCreditCardInfo extends CreditCardInfo {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getNumber() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setNumber(String number) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getExpDate() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setExpDate(String expDate) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getCvv() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setCvv(String cvv) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getStreetAddress() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setStreetAddress(String streetAddress) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getExtAddress() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setExtAddress(String extAddress) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getLocality() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setLocality(String locality) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getRegion() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setRegion(String region) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getPostalCode() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setPostalCode(String postalCode) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getCountry() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setCountry(String country) {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public String getAmount() {
        throw new UnsupportedOperationException("default credit card");
    }

    @Override
    public void setAmount(String amount) {
        throw new UnsupportedOperationException("default credit card");
    }
}
