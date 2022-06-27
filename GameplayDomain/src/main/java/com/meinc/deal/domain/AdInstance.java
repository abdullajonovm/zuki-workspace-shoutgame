package com.meinc.deal.domain;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import com.meinc.gameplay.domain.Localized;

public class AdInstance implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -873057703210815763L;
    public static final int classId = 2000;
    private int _id;
    private String _type;
    private String _consumerType;
    private int _width;
    private int _height;
    private String _imageUrl;
    private String _linkUrl;
    private String _offerText;
    private String _offerTextUuid;
    private List<Localized> _offerTextLocalized;
    private String _detailedInformation;
    private String _detailedInformationUuid;
    private List<Localized> _detailedInformationLocalized;
    
    @JsonProperty(value="adInstanceId")
    public int getId() {
        return _id;
    }

    @JsonProperty(value="adInstanceId")
    public void setId(int id) {
        _id = id;
    }
    
    public String getType() {
        return _type;
    }
    
    public void setType(String type) {
        _type = type;
    }
    
    public String getConsumerType() {
        return _consumerType;
    }
    
    public void setConsumerType(String consumerType) {
        _consumerType = consumerType;
    }
    
    public int getWidth() {
        return _width;
    }
    
    public void setWidth(int width) {
        _width = width;
    }
    
    public int getHeight() {
        return _height;
    }
    
    public void setHeight(int height) {
        _height = height;
    }
    
    public String getImageUrl() {
        return _imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        _imageUrl = imageUrl;
    }
    
    public String getLinkUrl() {
        return _linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        _linkUrl = linkUrl;
    }
    
    public String getOfferText() {
        return _offerText;
    }
    
    public void setOfferText(String offerText) {
        _offerText = offerText;
    }
    
    public String getDetailedInformation() {
        return _detailedInformation;
    }
    
    public void setDetailedInformation(String detailedInformation) {
        _detailedInformation = detailedInformation;
    }

    public String getOfferTextUuid()
    {
        return _offerTextUuid;
    }

    public void setOfferTextUuid(String offerTextUuid)
    {
        _offerTextUuid = offerTextUuid;
    }

    public List<Localized> getOfferTextLocalized()
    {
        return _offerTextLocalized;
    }

    public void setOfferTextLocalized(List<Localized> offerTextLocalized)
    {
        _offerTextLocalized = offerTextLocalized;
    }

    public String getDetailedInformationUuid()
    {
        return _detailedInformationUuid;
    }

    public void setDetailedInformationUuid(String detailedInformationUuid)
    {
        _detailedInformationUuid = detailedInformationUuid;
    }

    public List<Localized> getDetailedInformationLocalized()
    {
        return _detailedInformationLocalized;
    }

    public void setDetailedInformationLocalized(List<Localized> detailedInformationLocalized)
    {
        _detailedInformationLocalized = detailedInformationLocalized;
    }
}
