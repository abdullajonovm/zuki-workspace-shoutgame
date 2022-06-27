package com.meinc.gameplay.domain;

import java.io.Serializable;

public class Vendor
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _vendorId;
    /** short 3-4 characters */
    private String _abbr;

    /** fully qualified url; must be scalable for both question list icon and lineup screen titlebar icon */
    private String _iconUrl;

    /** in the form 'rrggbb' */
    private String _primaryColorRGB;

    /** in the form 'rrggbb' */
    private String _bgColorRGB;

    /** in the form 'rrggbb' */
    private String _highGradientColorRGB;

    /** in the form 'rrggbb' */
    private String _lowGradientColorRGB;

    /** in the form 'rrggbb' */
    private String _textColorRGB;

    /** standard def version of the checkbox image on the lineup screen, in the vendor's primary color */
    private String _checkboxSDUrl;

    /** high def version of the checkbox image on the lineup screen, in the vendor's primary color */
    private String _checkboxHDUrl;

    public Vendor()
    {
    }

    public int getVendorId()
    {
        return _vendorId;
    }

    public void setVendorId(int vendorId)
    {
        _vendorId = vendorId;
    }

    public String getAbbr()
    {
        return _abbr;
    }

    public void setAbbr(String abbr)
    {
        _abbr = abbr;
    }

    public String getIconUrl()
    {
        return _iconUrl;
    }

    public void setIconUrl(String iconUrl)
    {
        _iconUrl = iconUrl;
    }

    public String getPrimaryColorRGB()
    {
        return _primaryColorRGB;
    }

    public void setPrimaryColorRGB(String primaryColorRGB)
    {
        _primaryColorRGB = primaryColorRGB;
    }

    public String getBgColorRGB()
    {
        return _bgColorRGB;
    }

    public void setBgColorRGB(String bgColorRGB)
    {
        _bgColorRGB = bgColorRGB;
    }

    public String getHighGradientColorRGB()
    {
      return _highGradientColorRGB;
    }

    public void setHighGradientColorRGB(String highGradientColorRGB)
    {
      _highGradientColorRGB = highGradientColorRGB;
    }

    public String getLowGradientColorRGB()
    {
      return _lowGradientColorRGB;
    }

    public void setLowGradientColorRGB(String lowGradientColorRGB)
    {
      _lowGradientColorRGB = lowGradientColorRGB;
    }

    public String getTextColorRGB()
    {
      return _textColorRGB;
    }

    public void setTextColorRGB(String textColorRGB)
    {
      _textColorRGB = textColorRGB;
    }

    public String getCheckboxSDUrl()
    {
        return _checkboxSDUrl;
    }

    public void setCheckboxSDUrl(String checkboxSDUrl)
    {
        _checkboxSDUrl = checkboxSDUrl;
    }

    public String getCheckboxHDUrl()
    {
        return _checkboxHDUrl;
    }

    public void setCheckboxHDUrl(String checkboxHDUrl)
    {
        _checkboxHDUrl = checkboxHDUrl;
    }
}
