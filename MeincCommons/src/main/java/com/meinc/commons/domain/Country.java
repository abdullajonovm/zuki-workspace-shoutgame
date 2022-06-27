package com.meinc.commons.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Country implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;
  
	/** 
	 * If carrier is not speicified, don't put null into the database, instead
	 * use the default carrier code.  
	 */
	public static int DEFAULT_COUNTRY_NUMBER = 840; //US
	
	private static List<Country> _countryList;
	
	private String _name;
	private String _a2;
	private String _a3;
	private int _number;
  
	private static Map<Integer, Country> _countryMap = new HashMap<Integer, Country>();
	static
	{
		_countryMap.put(0, Country.getCountry("AU"));
		_countryMap.put(1, Country.getCountry("AT"));
		_countryMap.put(2, Country.getCountry("BE"));
		_countryMap.put(3, Country.getCountry("BR"));
		_countryMap.put(4, Country.getCountry("CA"));
		_countryMap.put(5, Country.getCountry("DK"));
		_countryMap.put(6, Country.getCountry("FI"));
		_countryMap.put(7, Country.getCountry("FR"));
		_countryMap.put(8, Country.getCountry("DE"));
		_countryMap.put(9, Country.getCountry("HK"));
		_countryMap.put(10, Country.getCountry("IS"));
		_countryMap.put(11, Country.getCountry("IE"));
		_countryMap.put(12, Country.getCountry("IT"));
		_countryMap.put(13, Country.getCountry("JP"));
		_countryMap.put(14, Country.getCountry("LU"));
		_countryMap.put(15, Country.getCountry("MX"));
		_countryMap.put(16, Country.getCountry("NL"));
		_countryMap.put(17, Country.getCountry("NZ"));
		_countryMap.put(18, Country.getCountry("NO"));
		_countryMap.put(19, Country.getCountry("ES"));
		_countryMap.put(20, Country.getCountry("SE"));
		_countryMap.put(21, Country.getCountry("CH"));
		_countryMap.put(22, Country.getCountry("GB"));
		_countryMap.put(23, Country.getCountry("US"));
		_countryMap.put(24, Country.getCountry("IN"));
		_countryMap.put(25, Country.getCountry("ID"));
		_countryMap.put(26, Country.getCountry("KR"));
		_countryMap.put(27, Country.getCountry("MY"));
		_countryMap.put(28, Country.getCountry("CN"));
		_countryMap.put(29, Country.getCountry("PH"));
		_countryMap.put(30, Country.getCountry("SG"));
		_countryMap.put(31, Country.getCountry("TH"));
		_countryMap.put(32, Country.getCountry("TW"));
		_countryMap.put(33, Country.getCountry("AD"));
		_countryMap.put(34, Country.getCountry("AE"));
		_countryMap.put(35, Country.getCountry("AF"));
		_countryMap.put(36, Country.getCountry("AG"));
		_countryMap.put(37, Country.getCountry("AI"));
		_countryMap.put(38, Country.getCountry("AL"));
		_countryMap.put(39, Country.getCountry("AM"));
		_countryMap.put(40, Country.getCountry("AN"));
		_countryMap.put(41, Country.getCountry("AO"));
		_countryMap.put(42, Country.getCountry("AQ"));
		_countryMap.put(43, Country.getCountry("AR"));
		_countryMap.put(44, Country.getCountry("AS"));
		_countryMap.put(45, Country.getCountry("AW"));
		_countryMap.put(46, Country.getCountry("AZ"));
		_countryMap.put(47, Country.getCountry("BA"));
		_countryMap.put(48, Country.getCountry("BB"));
		_countryMap.put(49, Country.getCountry("BD"));
		_countryMap.put(50, Country.getCountry("BF"));
		_countryMap.put(51, Country.getCountry("BG"));
		_countryMap.put(52, Country.getCountry("BH"));
		_countryMap.put(53, Country.getCountry("BI"));
		_countryMap.put(54, Country.getCountry("BJ"));
		_countryMap.put(55, Country.getCountry("BM"));
		_countryMap.put(56, Country.getCountry("BN"));
		_countryMap.put(57, Country.getCountry("BO"));
		_countryMap.put(58, Country.getCountry("BS"));
		_countryMap.put(59, Country.getCountry("BT"));
		_countryMap.put(60, Country.getCountry("BV"));
		_countryMap.put(61, Country.getCountry("BW"));
		_countryMap.put(62, Country.getCountry("BY"));
		_countryMap.put(63, Country.getCountry("BZ"));
		_countryMap.put(64, Country.getCountry("CC"));
		_countryMap.put(65, Country.getCountry("CD"));
		_countryMap.put(66, Country.getCountry("CF"));
		_countryMap.put(67, Country.getCountry("CG"));
		_countryMap.put(68, Country.getCountry("CI"));
		_countryMap.put(69, Country.getCountry("CK"));
		_countryMap.put(70, Country.getCountry("CL"));
		_countryMap.put(71, Country.getCountry("CM"));
		_countryMap.put(72, Country.getCountry("CO"));
		_countryMap.put(73, Country.getCountry("CR"));
		_countryMap.put(74, Country.getCountry("CU"));
		_countryMap.put(75, Country.getCountry("CV"));
		_countryMap.put(76, Country.getCountry("CX"));
		_countryMap.put(77, Country.getCountry("CY"));
		_countryMap.put(78, Country.getCountry("CZ"));
		_countryMap.put(79, Country.getCountry("DJ"));
		_countryMap.put(80, Country.getCountry("DM"));
		_countryMap.put(81, Country.getCountry("DO"));
		_countryMap.put(82, Country.getCountry("DZ"));
		_countryMap.put(83, Country.getCountry("EC"));
		_countryMap.put(84, Country.getCountry("EE"));
		_countryMap.put(85, Country.getCountry("EG"));
		_countryMap.put(86, Country.getCountry("EH"));
		_countryMap.put(87, Country.getCountry("ER"));
		_countryMap.put(88, Country.getCountry("ET"));
		_countryMap.put(89, Country.getCountry("FJ"));
		_countryMap.put(90, Country.getCountry("FK"));
		_countryMap.put(91, Country.getCountry("FM"));
		_countryMap.put(92, Country.getCountry("FO"));
		_countryMap.put(93, Country.getCountry("FX"));
		_countryMap.put(94, Country.getCountry("GA"));
		_countryMap.put(95, Country.getCountry("GD"));
		_countryMap.put(96, Country.getCountry("GE"));
		_countryMap.put(97, Country.getCountry("GF"));
		_countryMap.put(98, Country.getCountry("GH"));
		_countryMap.put(99, Country.getCountry("GI"));
		_countryMap.put(100, Country.getCountry("GL"));
		_countryMap.put(101, Country.getCountry("GM"));
		_countryMap.put(102, Country.getCountry("GN"));
		_countryMap.put(103, Country.getCountry("GP"));
		_countryMap.put(104, Country.getCountry("GQ"));
		_countryMap.put(105, Country.getCountry("GR"));
		_countryMap.put(106, Country.getCountry("GS"));
		_countryMap.put(107, Country.getCountry("GT"));
		_countryMap.put(108, Country.getCountry("GU"));
		_countryMap.put(109, Country.getCountry("GW"));
		_countryMap.put(110, Country.getCountry("GY"));
		_countryMap.put(111, Country.getCountry("HM"));
		_countryMap.put(112, Country.getCountry("HN"));
		_countryMap.put(113, Country.getCountry("HR"));
		_countryMap.put(114, Country.getCountry("HT"));
		_countryMap.put(115, Country.getCountry("HU"));
		_countryMap.put(116, Country.getCountry("IL"));
		_countryMap.put(117, Country.getCountry("IO"));
		_countryMap.put(118, Country.getCountry("IQ"));
		_countryMap.put(119, Country.getCountry("IR"));
		_countryMap.put(120, Country.getCountry("JM"));
		_countryMap.put(121, Country.getCountry("JO"));
		_countryMap.put(122, Country.getCountry("KE"));
		_countryMap.put(123, Country.getCountry("KG"));
		_countryMap.put(124, Country.getCountry("KH"));
		_countryMap.put(125, Country.getCountry("KI"));
		_countryMap.put(126, Country.getCountry("KM"));
		_countryMap.put(127, Country.getCountry("KN"));
		_countryMap.put(128, Country.getCountry("KP"));
		_countryMap.put(129, Country.getCountry("KW"));
		_countryMap.put(130, Country.getCountry("KY"));
		_countryMap.put(131, Country.getCountry("KK"));
		_countryMap.put(132, Country.getCountry("LA"));
		_countryMap.put(133, Country.getCountry("LB"));
		_countryMap.put(134, Country.getCountry("LC"));
		_countryMap.put(135, Country.getCountry("LI"));
		_countryMap.put(136, Country.getCountry("LK"));
		_countryMap.put(137, Country.getCountry("LR"));
		_countryMap.put(138, Country.getCountry("LS"));
		_countryMap.put(139, Country.getCountry("LT"));
		_countryMap.put(140, Country.getCountry("LV"));
		_countryMap.put(141, Country.getCountry("LY"));
		_countryMap.put(142, Country.getCountry("MA"));
		_countryMap.put(143, Country.getCountry("MC"));
		_countryMap.put(144, Country.getCountry("MD"));
		_countryMap.put(145, Country.getCountry("MG"));
		_countryMap.put(146, Country.getCountry("MH"));
		_countryMap.put(147, Country.getCountry("MK"));
		_countryMap.put(148, Country.getCountry("ML"));
		_countryMap.put(149, Country.getCountry("MM"));
		_countryMap.put(150, Country.getCountry("MN"));
		_countryMap.put(151, Country.getCountry("MO"));
		_countryMap.put(152, Country.getCountry("MP"));
		_countryMap.put(153, Country.getCountry("MQ"));
		_countryMap.put(154, Country.getCountry("MR"));
		_countryMap.put(155, Country.getCountry("MS"));
		_countryMap.put(156, Country.getCountry("MT"));
		_countryMap.put(157, Country.getCountry("MU"));
		_countryMap.put(158, Country.getCountry("MV"));
		_countryMap.put(159, Country.getCountry("MW"));
		_countryMap.put(160, Country.getCountry("MZ"));
		_countryMap.put(161, Country.getCountry("NA"));
		_countryMap.put(162, Country.getCountry("NC"));
		_countryMap.put(163, Country.getCountry("NE"));
		_countryMap.put(164, Country.getCountry("NF"));
		_countryMap.put(165, Country.getCountry("NG"));
		_countryMap.put(166, Country.getCountry("NI"));
		_countryMap.put(167, Country.getCountry("NP"));
		_countryMap.put(168, Country.getCountry("NR"));
		_countryMap.put(169, Country.getCountry("NU"));
		_countryMap.put(170, Country.getCountry("OM"));
		_countryMap.put(171, Country.getCountry("PA"));
		_countryMap.put(172, Country.getCountry("PE"));
		_countryMap.put(173, Country.getCountry("PF"));
		_countryMap.put(174, Country.getCountry("PG"));
		_countryMap.put(175, Country.getCountry("PK"));
		_countryMap.put(176, Country.getCountry("PL"));
		_countryMap.put(177, Country.getCountry("PM"));
		_countryMap.put(178, Country.getCountry("PN"));
		_countryMap.put(179, Country.getCountry("PR"));
		_countryMap.put(180, Country.getCountry("PT"));
		_countryMap.put(181, Country.getCountry("PW"));
		_countryMap.put(182, Country.getCountry("PY"));
		_countryMap.put(183, Country.getCountry("QA"));
		_countryMap.put(184, Country.getCountry("RE"));
		_countryMap.put(185, Country.getCountry("RO"));
		_countryMap.put(186, Country.getCountry("RU"));
		_countryMap.put(187, Country.getCountry("RW"));
		_countryMap.put(188, Country.getCountry("SA"));
		_countryMap.put(189, Country.getCountry("SB"));
		_countryMap.put(190, Country.getCountry("SC"));
		_countryMap.put(191, Country.getCountry("SD"));
		_countryMap.put(192, Country.getCountry("SH"));
		_countryMap.put(193, Country.getCountry("SI"));
		_countryMap.put(194, Country.getCountry("SJ"));
		_countryMap.put(195, Country.getCountry("SK"));
		_countryMap.put(196, Country.getCountry("SL"));
		_countryMap.put(197, Country.getCountry("SM"));
		_countryMap.put(198, Country.getCountry("SN"));
		_countryMap.put(199, Country.getCountry("SO"));
		_countryMap.put(200, Country.getCountry("SR"));
		_countryMap.put(201, Country.getCountry("ST"));
		_countryMap.put(202, Country.getCountry("SV"));
		_countryMap.put(203, Country.getCountry("SY"));
		_countryMap.put(204, Country.getCountry("SZ"));
		_countryMap.put(205, Country.getCountry("TC"));
		_countryMap.put(206, Country.getCountry("TD"));
		_countryMap.put(207, Country.getCountry("TF"));
		_countryMap.put(208, Country.getCountry("TG"));
		_countryMap.put(209, Country.getCountry("TJ"));
		_countryMap.put(210, Country.getCountry("TK"));
		_countryMap.put(211, Country.getCountry("TM"));
		_countryMap.put(212, Country.getCountry("TN"));
		_countryMap.put(213, Country.getCountry("TO"));
		_countryMap.put(214, Country.getCountry("TP"));
		_countryMap.put(215, Country.getCountry("TR"));
		_countryMap.put(216, Country.getCountry("TT"));
		_countryMap.put(217, Country.getCountry("TV"));
		_countryMap.put(218, Country.getCountry("TZ"));
		_countryMap.put(219, Country.getCountry("UA"));
		_countryMap.put(220, Country.getCountry("UG"));
		_countryMap.put(221, Country.getCountry("UM"));
		_countryMap.put(222, Country.getCountry("UY"));
		_countryMap.put(223, Country.getCountry("UZ"));
		_countryMap.put(224, Country.getCountry("VA"));
		_countryMap.put(225, Country.getCountry("VC"));
		_countryMap.put(226, Country.getCountry("VE"));
		_countryMap.put(227, Country.getCountry("VG"));
		_countryMap.put(228, Country.getCountry("VI"));
		_countryMap.put(229, Country.getCountry("VN"));
		_countryMap.put(230, Country.getCountry("VU"));
		_countryMap.put(231, Country.getCountry("WF"));
		_countryMap.put(232, Country.getCountry("WS"));
		_countryMap.put(233, Country.getCountry("YE"));
		_countryMap.put(234, Country.getCountry("YT"));
		_countryMap.put(235, Country.getCountry("YU"));
		_countryMap.put(236, Country.getCountry("ZA"));
		_countryMap.put(237, Country.getCountry("ZM"));
		_countryMap.put(238, Country.getCountry("ZW"));	
	}
	
  public Country(String name, String a2, String a3, int number)
  {
  	_name = name;
  	_a2 = a2;
  	_a3 = a3;
  	_number = number;
  }

  public String toString()
  {
  	return "name - " + _name + ", " + "a2 - " + _a2;
  }
  
	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getA2()
	{
		return _a2;
	}

	public void setA2(String a2)
	{
		_a2 = a2;
	}

	public String getA3()
	{
		return _a3;
	}

	public void setA3(String a3)
	{
		_a3 = a3;
	}

	public int getNumber()
	{
		return _number;
	}

	public void setNumber(int number)
	{
		_number = number;
	}
	
	/**
	 * Map a Palm Country code to a Country object.
	 */
	public static Country getCountryFromPhoneInput(int palmCountryCode)
	{
		Country c = _countryMap.get(palmCountryCode);
		if (c == null)
			c = Country.getCounty(Country.DEFAULT_COUNTRY_NUMBER);
		return c;
	}
	
  /**
   * Returns the country object from the country list by case insensitive
   * country name.
   *
   * @param name The name of the country whose object we want.
   * @return Country The country matching the name or null if none matched.
   */
  public static Country getCountryByName(String name)
  {
    Country result = null;
    
    for (Country country : getCountryList())
    {
      if (country._name.equalsIgnoreCase(name))
      {
        result = country;
        break;
      }
    }
    
    return result;
  }
  
  public static Country getCountry(String a)
  {
  	Country result = null;
  	if (a.length() == 2) {
  		for (Country country: getCountryList())
  		{
  			if (country._a2.equalsIgnoreCase(a))
  			{
  				result = country;
  				break;
  			}
  		}
  	} else if (a.length() == 3) {
  		for (Country country: getCountryList())
  		{
  			if (country._a3.equalsIgnoreCase(a))
  			{
  				result = country;
  				break;
  			}
  		}
  	}
  	
  	return result;
  }

  public static Country getCounty(int number)
  {
  	Country result = null;
  	for (Country country: getCountryList())
  	{
  		if (country._number == number)
  		{
  			result = country;
  			break;
  		}
  	}
  	
  	return result;
  }

	public static List<Country> getCountryList()
	{   
		if (_countryList == null)
		{
    	_countryList = new ArrayList<Country>();
    	_countryList.add(new Country("Aaland Islands", "AX", "ALA", 248));
    	_countryList.add(new Country("Afghanistan", "AF", "AFG", 4));
    	_countryList.add(new Country("Albania", "AL", "ALB", 8));
    	_countryList.add(new Country("Algeria", "DZ", "DZA", 12));
    	_countryList.add(new Country("American Samoa", "AS", "ASM", 16));
    	_countryList.add(new Country("Andorra", "AD", "AND", 20));
    	_countryList.add(new Country("Angola", "AO", "AGO", 24));
    	_countryList.add(new Country("Anguilla", "AI", "AIA", 660));
    	_countryList.add(new Country("Antarctica", "AQ", "ATA", 10));
    	_countryList.add(new Country("Antigua and Barbuda", "AG", "ATG", 28));
    	_countryList.add(new Country("Argentina", "AR", "ARG", 32));
    	_countryList.add(new Country("Armenia", "AM", "ARM", 51));
    	_countryList.add(new Country("Aruba", "AW", "ABW", 533));
    	_countryList.add(new Country("Australia", "AU", "AUS", 36));
    	_countryList.add(new Country("Austria", "AT", "AUT", 40));
    	_countryList.add(new Country("Azerbaijan", "AZ", "AZE", 31));
    	_countryList.add(new Country("Bahamas", "BS", "BHS", 44));
    	_countryList.add(new Country("Bahrain", "BH", "BHR", 48));
    	_countryList.add(new Country("Bangladesh", "BD", "BGD", 50));
    	_countryList.add(new Country("Barbados", "BB", "BRB", 52));
    	_countryList.add(new Country("Belarus", "BY", "BLR", 112));
    	_countryList.add(new Country("Belgium", "BE", "BEL", 56));
    	_countryList.add(new Country("Belize", "BZ", "BLZ", 84));
    	_countryList.add(new Country("Benin", "BJ", "BEN", 204));
    	_countryList.add(new Country("Bermuda", "BM", "BMU", 60));
    	_countryList.add(new Country("Bhutan", "BT", "BTN", 64));
    	_countryList.add(new Country("Bolivia", "BO", "BOL", 68));
    	_countryList.add(new Country("Bosnia and Herzegowina", "BA", "BIH", 70));
    	_countryList.add(new Country("Botswana", "BW", "BWA", 72));
    	_countryList.add(new Country("Bouvet Island", "BV", "BVT", 74));
    	_countryList.add(new Country("Brazil", "BR", "BRA", 76));
    	_countryList.add(new Country("British Indian Ocean Territory", "IO", "IOT", 86));
    	_countryList.add(new Country("Brunei Darussalam", "BN", "BRN", 96));
    	_countryList.add(new Country("Bulgaria", "BG", "BGR", 100));
    	_countryList.add(new Country("Burkina Faso", "BF", "BFA", 854));
    	_countryList.add(new Country("Burundi", "BI", "BDI", 108));
    	_countryList.add(new Country("Cambodia", "KH", "KHM", 116));
    	_countryList.add(new Country("Cameroon", "CM", "CMR", 120));
    	_countryList.add(new Country("Canada", "CA", "CAN", 124));
    	_countryList.add(new Country("Cape Verde", "CV", "CPV", 132));
    	_countryList.add(new Country("Cayman Islands", "KY", "CYM", 136));
    	_countryList.add(new Country("Central African Republic", "CF", "CAF", 140));
    	_countryList.add(new Country("Chad", "TD", "TCD", 148));
    	_countryList.add(new Country("Chile", "CL", "CHL", 152));
    	_countryList.add(new Country("China", "CN", "CHN", 156));
    	_countryList.add(new Country("Christmas Island", "CX", "CXR", 162));
    	_countryList.add(new Country("Cocos (Keeling) Islands", "CC", "CCK", 166));
    	_countryList.add(new Country("Colombia", "CO", "COL", 170));
    	_countryList.add(new Country("Comoros", "KM", "COM", 174));
    	_countryList.add(new Country("Congo, Democratic Republic of", "CD", "COD", 180));
    	_countryList.add(new Country("Congo, Republic of", "CG", "COG", 178));
    	_countryList.add(new Country("Cook Islands", "CK", "COK", 184));
    	_countryList.add(new Country("Costa Rica", "CR", "CRI", 188));
    	_countryList.add(new Country("Cote D'Ivoire", "CI", "CIV", 384));
    	_countryList.add(new Country("Croatia", "HR", "HRV", 191));
    	_countryList.add(new Country("Cuba", "CU", "CUB", 192));
    	_countryList.add(new Country("Cyprus", "CY", "CYP", 196));
    	_countryList.add(new Country("Czech Republic", "CZ", "CZE", 203));
    	_countryList.add(new Country("Denmark", "DK", "DNK", 208));
    	_countryList.add(new Country("Djibouti", "DJ", "DJI", 262));
    	_countryList.add(new Country("Dominica", "DM", "DMA", 212));
    	_countryList.add(new Country("Dominican Republic", "DO", "DOM", 214));
    	_countryList.add(new Country("Ecuador", "EC", "ECU", 218));
    	_countryList.add(new Country("Egypt", "EG", "EGY", 818));
    	_countryList.add(new Country("El Salvador", "SV", "SLV", 222));
    	_countryList.add(new Country("Equatorial Guinea", "GQ", "GNQ", 226));
    	_countryList.add(new Country("Eritrea", "ER", "ERI", 232));
    	_countryList.add(new Country("Estonia", "EE", "EST", 233));
    	_countryList.add(new Country("Ethiopia", "ET", "ETH", 231));
    	_countryList.add(new Country("Falkland Islands (Malvinas)", "FK", "FLK", 238));
    	_countryList.add(new Country("Faroe Islands", "FO", "FRO", 234));
    	_countryList.add(new Country("Fiji", "FJ", "FJI", 242));
    	_countryList.add(new Country("Finland", "FI", "FIN", 246));
    	_countryList.add(new Country("France", "FR", "FRA", 250));
    	_countryList.add(new Country("French Guiana", "GF", "GUF", 254));
    	_countryList.add(new Country("French Polynesia", "PF", "PYF", 258));
    	_countryList.add(new Country("French Southern Territories", "TF", "ATF", 260));
    	_countryList.add(new Country("Gabon", "GA", "GAB", 266));
    	_countryList.add(new Country("Gambia", "GM", "GMB", 270));
    	_countryList.add(new Country("Georgia", "GE", "GEO", 268));
    	_countryList.add(new Country("Germany", "DE", "DEU", 276));
    	_countryList.add(new Country("Ghana", "GH", "GHA", 288));
    	_countryList.add(new Country("Gibraltar", "GI", "GIB", 292));
    	_countryList.add(new Country("Greece", "GR", "GRC", 300));
    	_countryList.add(new Country("Greenland", "GL", "GRL", 304));
    	_countryList.add(new Country("Grenada", "GD", "GRD", 308));
    	_countryList.add(new Country("Guadeloupe", "GP", "GLP", 312));
    	_countryList.add(new Country("Guam", "GU", "GUM", 316));
    	_countryList.add(new Country("Guatemala", "GT", "GTM", 320));
    	_countryList.add(new Country("Guinea", "GN", "GIN", 324));
    	_countryList.add(new Country("Guinea-Bissau", "GW", "GNB", 624));
    	_countryList.add(new Country("Guyana", "GY", "GUY", 328));
    	_countryList.add(new Country("Haiti", "HT", "HTI", 332));
    	_countryList.add(new Country("Heard and Mc Donald Islands", "HM", "HMD", 334));
    	_countryList.add(new Country("Honduras", "HN", "HND", 340));
    	_countryList.add(new Country("Hong Kong", "HK", "HKG", 344));
    	_countryList.add(new Country("Hungary", "HU", "HUN", 348));
    	_countryList.add(new Country("Iceland", "IS", "ISL", 352));
    	_countryList.add(new Country("India", "IN", "IND", 356));
    	_countryList.add(new Country("Indonesia", "ID", "IDN", 360));
    	_countryList.add(new Country("Iran, Islamic Republic of", "IR", "IRN", 364));
    	_countryList.add(new Country("Iraq", "IQ", "IRQ", 368));
    	_countryList.add(new Country("Ireland", "IE", "IRL", 372));
    	_countryList.add(new Country("Israel", "IL", "ISR", 376));
    	_countryList.add(new Country("Italy", "IT", "ITA", 380));
    	_countryList.add(new Country("Jamaica", "JM", "JAM", 388));
    	_countryList.add(new Country("Japan", "JP", "JPN", 392));
    	_countryList.add(new Country("Jordan", "JO", "JOR", 400));
    	_countryList.add(new Country("Kazakhstan", "KZ", "KAZ", 398));
    	_countryList.add(new Country("Kenya", "KE", "KEN", 404));
    	_countryList.add(new Country("Kiribati", "KI", "KIR", 296));
    	_countryList.add(new Country("Korea, Democratic People's Republic of", "KP", "PRK", 408));
    	_countryList.add(new Country("Korea, Republic of", "KR", "KOR", 410));
    	_countryList.add(new Country("Kuwait", "KW", "KWT", 414));
    	_countryList.add(new Country("Kyrgyzstan", "KG", "KGZ", 417));
    	_countryList.add(new Country("Lao People's Democratic Republic", "LA", "LAO", 418));
    	_countryList.add(new Country("Latvia", "LV", "LVA", 428));
    	_countryList.add(new Country("Lebanon", "LB", "LBN", 422));
    	_countryList.add(new Country("Lesotho", "LS", "LSO", 426));
    	_countryList.add(new Country("Liberia", "LR", "LBR", 430));
    	_countryList.add(new Country("Libyan Arab Jamahiriya", "LY", "LBY", 434));
    	_countryList.add(new Country("Liechtenstein", "LI", "LIE", 438));
    	_countryList.add(new Country("Lithuania", "LT", "LTU", 440));
    	_countryList.add(new Country("Luxembourg", "LU", "LUX", 442));
    	_countryList.add(new Country("Macau", "MO", "MAC", 446));
    	_countryList.add(new Country("Macedonia, The Former Yugoslav Republic of", "MK", "MKD", 807));
    	_countryList.add(new Country("Madagascar", "MG", "MDG", 450));
    	_countryList.add(new Country("Malawi", "MW", "MWI", 454));
    	_countryList.add(new Country("Malaysia", "MY", "MYS", 458));
    	_countryList.add(new Country("Maldives", "MV", "MDV", 462));
    	_countryList.add(new Country("Mali", "ML", "MLI", 466));
    	_countryList.add(new Country("Malta", "MT", "MLT", 470));
    	_countryList.add(new Country("Marshall Islands", "MH", "MHL", 584));
    	_countryList.add(new Country("Martinique", "MQ", "MTQ", 474));
    	_countryList.add(new Country("Mauritania", "MR", "MRT", 478));
    	_countryList.add(new Country("Mauritius", "MU", "MUS", 480));
    	_countryList.add(new Country("Mayotte", "YT", "MYT", 175));
    	_countryList.add(new Country("Mexico", "MX", "MEX", 484));
    	_countryList.add(new Country("Micronesia, Federated States of", "FM", "FSM", 583));
    	_countryList.add(new Country("Moldova, Republic of", "MD", "MDA", 498));
    	_countryList.add(new Country("Monaco", "MC", "MCO", 492));
    	_countryList.add(new Country("Mongolia", "MN", "MNG", 496));
    	_countryList.add(new Country("Montserrat", "MS", "MSR", 500));
    	_countryList.add(new Country("Morocco", "MA", "MAR", 504));
    	_countryList.add(new Country("Mozambique", "MZ", "MOZ", 508));
    	_countryList.add(new Country("Myanmar", "MM", "MMR", 104));
    	_countryList.add(new Country("Namibia", "NA", "NAM", 516));
    	_countryList.add(new Country("Nauru", "NR", "NRU", 520));
    	_countryList.add(new Country("Nepal", "NP", "NPL", 524));
    	_countryList.add(new Country("Netherlands", "NL", "NLD", 528));
    	_countryList.add(new Country("Netherlands Antilles", "AN", "ANT", 530));
    	_countryList.add(new Country("New Caledonia", "NC", "NCL", 540));
    	_countryList.add(new Country("New Zealand", "NZ", "NZL", 554));
    	_countryList.add(new Country("Nicaragua", "NI", "NIC", 558));
    	_countryList.add(new Country("Niger", "NE", "NER", 562));
    	_countryList.add(new Country("Nigeria", "NG", "NGA", 566));
    	_countryList.add(new Country("Niue", "NU", "NIU", 570));
    	_countryList.add(new Country("Norfolk Island", "NF", "NFK", 574));
    	_countryList.add(new Country("Northern Mariana Islands", "MP", "MNP", 580));
    	_countryList.add(new Country("Norway", "NO", "NOR", 578));
    	_countryList.add(new Country("Oman", "OM", "OMN", 512));
    	_countryList.add(new Country("Pakistan", "PK", "PAK", 586));
    	_countryList.add(new Country("Palau", "PW", "PLW", 585));
    	_countryList.add(new Country("Palestinian Territory, Occupied", "PS", "PSE", 275));
    	_countryList.add(new Country("Panama", "PA", "PAN", 591));
    	_countryList.add(new Country("Papua New Guinea", "PG", "PNG", 598));
    	_countryList.add(new Country("Paraguay", "PY", "PRY", 600));
    	_countryList.add(new Country("Peru", "PE", "PER", 604));
    	_countryList.add(new Country("Philippines", "PH", "PHL", 608));
    	_countryList.add(new Country("Pitcairn", "PN", "PCN", 612));
    	_countryList.add(new Country("Poland", "PL", "POL", 616));
    	_countryList.add(new Country("Portugal", "PT", "PRT", 620));
    	_countryList.add(new Country("Puerto Rico", "PR", "PRI", 630));
    	_countryList.add(new Country("Qatar", "QA", "QAT", 634));
    	_countryList.add(new Country("Reunion", "RE", "REU", 638));
    	_countryList.add(new Country("Romania", "RO", "ROU", 642));
    	_countryList.add(new Country("Russian Federation", "RU", "RUS", 643));
    	_countryList.add(new Country("Rwanda", "RW", "RWA", 646));
    	_countryList.add(new Country("Saint Helena", "SH", "SHN", 654));
    	_countryList.add(new Country("Saint Kitts and Nevis", "KN", "KNA", 659));
    	_countryList.add(new Country("Saint Lucia", "LC", "LCA", 662));
    	_countryList.add(new Country("Saint Pierre and Miquelon", "PM", "SPM", 666));
    	_countryList.add(new Country("Saint Vincent and The Grenadines", "VC", "VCT", 670));
    	_countryList.add(new Country("Samoa", "WS", "WSM", 882));
    	_countryList.add(new Country("San Marino", "SM", "SMR", 674));
    	_countryList.add(new Country("Sao Tome and Principe", "ST", "STP", 678));
    	_countryList.add(new Country("Saudi Arabia", "SA", "SAU", 682));
    	_countryList.add(new Country("Senegal", "SN", "SEN", 686));
    	_countryList.add(new Country("Serbia and Montenegro", "CS", "SCG", 891));
    	_countryList.add(new Country("Seychelles", "SC", "SYC", 690));
    	_countryList.add(new Country("Sierra Leone", "SL", "SLE", 694));
    	_countryList.add(new Country("Singapore", "SG", "SGP", 702));
    	_countryList.add(new Country("Slovakia", "SK", "SVK", 703));
    	_countryList.add(new Country("Slovenia", "SI", "SVN", 705));
    	_countryList.add(new Country("Solomon Islands", "SB", "SLB", 90));
    	_countryList.add(new Country("Somalia", "SO", "SOM", 706));
    	_countryList.add(new Country("South Africa", "ZA", "ZAF", 710));
    	_countryList.add(new Country("South Georgia and The South Sandwich Islands", "GS", "SGS", 239));
    	_countryList.add(new Country("Spain", "ES", "ESP", 724));
    	_countryList.add(new Country("Sri Lanka", "LK", "LKA", 144));
    	_countryList.add(new Country("Sudan", "SD", "SDN", 736));
    	_countryList.add(new Country("Suriname", "SR", "SUR", 740));
    	_countryList.add(new Country("Svalbard and Jan Mayen Islands", "SJ", "SJM", 744));
    	_countryList.add(new Country("Swaziland", "SZ", "SWZ", 748));
    	_countryList.add(new Country("Sweden", "SE", "SWE", 752));
    	_countryList.add(new Country("Switzerland", "CH", "CHE", 756));
    	_countryList.add(new Country("Syrian Arab Republic", "SY", "SYR", 760));
    	_countryList.add(new Country("Taiwan", "TW", "TWN", 158));
    	_countryList.add(new Country("Tajikistan", "TJ", "TJK", 762));
    	_countryList.add(new Country("Tanzania, United Republic of", "TZ", "TZA", 834));
    	_countryList.add(new Country("Thailand", "TH", "THA", 764));
    	_countryList.add(new Country("Timor-Leste", "TL", "TLS", 626));
    	_countryList.add(new Country("Togo", "TG", "TGO", 768));
    	_countryList.add(new Country("Tokelau", "TK", "TKL", 772));
    	_countryList.add(new Country("Tonga", "TO", "TON", 776));
    	_countryList.add(new Country("Trinidad and Tobago", "TT", "TTO", 780));
    	_countryList.add(new Country("Tunisia", "TN", "TUN", 788));
    	_countryList.add(new Country("Turkey", "TR", "TUR", 792));
    	_countryList.add(new Country("Turkmenistan", "TM", "TKM", 795));
    	_countryList.add(new Country("Turks and Caicos Islands", "TC", "TCA", 796));
    	_countryList.add(new Country("Tuvalu", "TV", "TUV", 798));
    	_countryList.add(new Country("Uganda", "UG", "UGA", 800));
    	_countryList.add(new Country("Ukraine", "UA", "UKR", 804));
    	_countryList.add(new Country("United Arab Emirates", "AE", "ARE", 784));
    	_countryList.add(new Country("United Kingdom", "GB", "GBR", 826));
    	_countryList.add(new Country("United States", "US", "USA", 840));
    	_countryList.add(new Country("United States Minor Outlying Islands", "UM", "UMI", 581));
    	_countryList.add(new Country("Uruguay", "UY", "URY", 858));
    	_countryList.add(new Country("Uzbekistan", "UZ", "UZB", 860));
    	_countryList.add(new Country("Vanuatu", "VU", "VUT", 548));
    	_countryList.add(new Country("Vatican City State (Holy See)", "VA", "VAT", 336));
    	_countryList.add(new Country("Venezuela", "VE", "VEN", 862));
    	_countryList.add(new Country("Viet Nam", "VN", "VNM", 704));
    	_countryList.add(new Country("Virgin Islands (British)", "VG", "VGB", 92));
    	_countryList.add(new Country("Virgin Islands (U.S.)", "VI", "VIR", 850));
    	_countryList.add(new Country("Wallis and Futuna Islands", "WF", "WLF", 876));
    	_countryList.add(new Country("Western Sahara", "EH", "ESH", 732));
    	_countryList.add(new Country("Yemen", "YE", "YEM", 887));
    	_countryList.add(new Country("Zambia", "ZM", "ZMB", 894));
    	_countryList.add(new Country("Zimbabwe", "ZW", "ZWE", 716));
  	}
  	
    return _countryList;
  }  
  
  @Override
  public Country clone() throws CloneNotSupportedException
  {
  	return (Country) super.clone();
  }	
}
