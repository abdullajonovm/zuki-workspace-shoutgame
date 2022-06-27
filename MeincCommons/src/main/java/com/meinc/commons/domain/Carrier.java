package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Carrier implements Serializable, Cloneable
{
  private static final long serialVersionUID = 1L;

  /**
   * If carrier is not speicified, don't put null into the database, instead
   * use the default carrier code.
   */
  public static int DEFAULT_CARRIER_CODE = 9;

  private String _name;
  private int _value;
  private String _emailAddr;

  private static List<Carrier> _carrierList;
  private static Carrier _defaultCarrier;

	private static Map<String, Carrier> _carrierMccMncMap = new HashMap<String, Carrier>();
	static
	{
		_carrierMccMncMap.put("310150", Carrier.getCarrierByName("Cingular"));
		_carrierMccMncMap.put("310180", Carrier.getCarrierByName("Cingular"));
		_carrierMccMncMap.put("310380", Carrier.getCarrierByName("Cingular"));
		_carrierMccMncMap.put("310410", Carrier.getCarrierByName("Cingular"));
		_carrierMccMncMap.put("311180", Carrier.getCarrierByName("Cingular"));
		_carrierMccMncMap.put("310012",  Carrier.getCarrierByName("Verizon"));
		_carrierMccMncMap.put("310020",  Carrier.getCarrierByName("Sprint"));
		_carrierMccMncMap.put("310160",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310200",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310210",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310220",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310230",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310240",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310250",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310260",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310270",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310280",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310290",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310300",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310310",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310660",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("310800",  Carrier.getCarrierByName("T-Mobile US"));
		_carrierMccMncMap.put("26201",  Carrier.getCarrierByName("T-Mobile Germany"));
		_carrierMccMncMap.put("26206",  Carrier.getCarrierByName("T-Mobile Germany"));
		_carrierMccMncMap.put("26202",  Carrier.getCarrierByName("Vodafone Germany"));
		_carrierMccMncMap.put("26204",  Carrier.getCarrierByName("Vodafone Germany"));
		_carrierMccMncMap.put("26209",  Carrier.getCarrierByName("Vodafone Germany"));
		_carrierMccMncMap.put("26203",  Carrier.getCarrierByName("E-Plus Germany"));
		_carrierMccMncMap.put("26205",  Carrier.getCarrierByName("E-Plus Germany"));
		_carrierMccMncMap.put("26277",  Carrier.getCarrierByName("E-Plus Germany"));
		_carrierMccMncMap.put("722341",  Carrier.getCarrierByName("Personal Argentina"));
		_carrierMccMncMap.put("23402",  Carrier.getCarrierByName("O2 UK"));
		_carrierMccMncMap.put("23410",  Carrier.getCarrierByName("O2 UK"));
		_carrierMccMncMap.put("23411",  Carrier.getCarrierByName("O2 UK"));
		_carrierMccMncMap.put("23430",  Carrier.getCarrierByName("T-Mobile UK"));
		_carrierMccMncMap.put("23433",  Carrier.getCarrierByName("Orange UK"));
		_carrierMccMncMap.put("23434",  Carrier.getCarrierByName("Orange UK"));
		_carrierMccMncMap.put("23415",  Carrier.getCarrierByName("Vodafone UK"));
		_carrierMccMncMap.put("302651",  Carrier.getCarrierByName("Bell Canada"));
		_carrierMccMncMap.put("302720",  Carrier.getCarrierByName("Rogers Canada"));
		_carrierMccMncMap.put("302360",  Carrier.getCarrierByName("Telus Canada"));
		_carrierMccMncMap.put("302361",  Carrier.getCarrierByName("Telus Canada"));
		_carrierMccMncMap.put("302653",  Carrier.getCarrierByName("Telus Canada"));
		_carrierMccMncMap.put("302657",  Carrier.getCarrierByName("Telus Canada"));
		_carrierMccMncMap.put("20801",  Carrier.getCarrierByName("Orange France"));
		_carrierMccMncMap.put("20802",  Carrier.getCarrierByName("Orange France"));
	}
  
	/**
	 * The following rules will be applied:
	 * <ol>
	 * 	<li>case insensitive check for the substring "verizon" or "sprint" or "bell" - in the case of US CDMA networks</li>
	 * 	<li>check for a match on the MCC/MNC code (first 3/last2-3) against the http://en.wikipedia.org/wiki/Mobile_Network_Code website</li>
	 *  <li>assume id 9 - no carrier</li>
	 * </ol>
	 * @return
	 */
	public static Carrier getCarrierIdFromPhoneInput(String s)
	{
		if (s == null) return Carrier.getDefaultCarrier();
		s = s.toLowerCase();
		
		//check for US CDMA networks
		if (s.indexOf("bell") != -1) return Carrier.getCarrierByName("Cingular");
		if (s.indexOf("sprint") != -1) return Carrier.getCarrierByName("Sprint");
		if (s.indexOf("verizon") != -1) return Carrier.getCarrierByName("Verizon");
		
		Carrier c = _carrierMccMncMap.get(s);
		if (c == null) c = Carrier.getDefaultCarrier();
		return c;
	}
	
  public static List<Carrier> getCarrierList()
  {
    if (_carrierList == null)
    {
    	_defaultCarrier = new Carrier("Send Me Email", 9, "");

      _carrierList = new ArrayList<Carrier>();
      _carrierList.add(_defaultCarrier);
      _carrierList.add(new Carrier("Cingular", 13, "mobile.mycingular.net"));
      _carrierList.add(new Carrier("Verizon", 18, "vtext.com"));
      _carrierList.add(new Carrier("Sprint", 16, "messaging.sprintpcs.com"));
      _carrierList.add(new Carrier("T-Mobile US", 17, "tmomail.net"));
      _carrierList.add(new Carrier("T-Mobile Germany", 19, "t-d1-sms.de"));
      _carrierList.add(new Carrier("Vodafone Germany", 20, "vodafone-sms.de"));
      _carrierList.add(new Carrier("E-Plus Germany", 30, "smsmail.eplus.de"));
      _carrierList.add(new Carrier("Personal Argentina", 21, "personal-net.com.ar"));
      _carrierList.add(new Carrier("O2 UK", 22, "mmail.co.uk"));
      _carrierList.add(new Carrier("T-Mobile UK", 23, "t-mobile.uk.net"));
      _carrierList.add(new Carrier("Orange UK", 24, "orange.net"));
      _carrierList.add(new Carrier("Vodafone UK", 29, "vodafone.net"));
      _carrierList.add(new Carrier("Bell Canada", 25, "txt.bellmobility.ca"));
      _carrierList.add(new Carrier("Rogers Canada", 26, "pcs.rogers.com"));
      _carrierList.add(new Carrier("Telus Canada", 27, "msg.telus.com"));
      _carrierList.add(new Carrier("Orange France", 28, "orange.fr"));
    }

    return _carrierList;
  }

  public static Carrier getDefaultCarrier()
  {
    // Call ensure variables are initialized
    getCarrierList();

    return _defaultCarrier;
  }

  /**
   * Returns the carrier object from the carrier list by case insensitive
   * carrier name.
   *
   * @param carrierName The name of the carrier whose object we want.
   * @return Carrier The carrier matching the name or null if none matched.
   */
  public static Carrier getCarrierByName(String carrierName)
  {
    Carrier result = null;
    List<Carrier> carriers = getCarrierList();

    for (Carrier carrier : carriers)
    {
      if (carrier.getName().equalsIgnoreCase(carrierName))
      {
        result = carrier;
        break;
      }
    }

    return result;
  }

  public static Carrier getCarrier(int value)
  {
    Carrier result = null;

    for (Carrier carrier: getCarrierList())
    {
      if (carrier.getValue() == value)
      {
        result = carrier;
        break;
      }
    }

    return result;
  }

  public String toString()
  {
    return "name - " + _name + ", " + "value - " + _value;
  }

  @Override
  public boolean equals(Object object)
  {
    if (object == null)
      return false;
    if (!(object instanceof Carrier))
      return false;

    Carrier carrier = (Carrier) object;
  	return this.getValue() == carrier.getValue();
  }

  public Carrier(String name, int value, String emailAddr)
  {
    setName(name);
    setValue(value);
    setEmailAddr(emailAddr);
  }

  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }

  public int getValue()
  {
    return _value;
  }

  public void setValue(int value)
  {
    _value = value;
  }

  public String getEmailAddr()
  {
    return _emailAddr;
  }

  public void setEmailAddr(String emailAddr)
  {
    _emailAddr = emailAddr;
  }

  @Override
  public Carrier clone() throws CloneNotSupportedException
  {
  	return (Carrier) super.clone();
  }

//  public static void main(String[] args)
//	{
//		try {
//			String carrierName = "Sprint";
//			Carrier carrier = Carrier.getCarrierByName(carrierName);
//			System.out.println("carrier: " + carrier);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
