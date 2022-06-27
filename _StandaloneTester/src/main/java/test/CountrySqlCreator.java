package test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class CountrySqlCreator
{
    public static final String INSERT_MULTILOCALIZATION_TEMPLATE =
        "insert into contest.multi_localization (uuid, `type`, language_code, `value`) values (''f983f72c-e72c-4a69-be9a-1ab53276ecc5'', ''cc_{0}'', ''en'', ''{1}'');";

    public static final String INSERT_COUNTRY_CODES_TEMPLATE =
        "insert into contest.countries (country_code, dial_code, sort_order) values (''{0}'', {1,number,#}, {2,number,#});";

    public static final List<String> COUNTRY_CODES = Arrays.asList(
        "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AR", "AS", "AT", "AU", "AW", "AZ", "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM",
        "BN", "BO", "BR", "BS", "BT", "BW", "BY", "BZ", "CA", "CD", "CF", "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR", "CU", "CV", "CW", "CY", "CZ", "DE", "DJ",
        "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "ER", "ES", "ET", "FI", "FJ", "FK", "FM", "FO", "FR", "GA", "GB", "GD", "GE", "GH", "GI", "GL", "GM", "GN", "GQ",
        "GR", "GT", "GU", "GW", "GY", "HK", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IN", "IQ", "IR", "IS", "IT", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM",
        "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MK", "ML",
        "MM", "MN", "MO", "MP", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NC", "NE", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA",
        "PE", "PF", "PG", "PH", "PK", "PL", "PM", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI",
        "SK", "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SV", "SX", "SY", "SZ", "TC", "TD", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW",
        "TZ", "UA", "UG", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW"
    );

    public static final List<String> COUNTRY_NAMES_EN = Arrays.asList(
        "Andorra", "United Arab Emirates", "Afghanistan", "Antigua and Barbuda", "Anguilla", "Albania", "Armenia", "Netherlands Antilles", "Angola", "Argentina",
        "American Samoa", "Austria", "Australia", "Aruba", "Azerbaijan", "Bosnia and Herzegovina", "Barbados", "Bangladesh", "Belgium", "Burkina Faso", "Bulgaria",
        "Bahrain", "Burundi", "Benin", "Saint Barthelemy", "Bermuda", "Brunei", "Bolivia", "Brazil", "Bahamas", "Bhutan", "Botswana", "Belarus", "Belize", "Canada",
        "Democratic Republic of Congo", "Central African Republic", "Switzerland", "Ivory Coast", "Cook Islands", "Chile", "Cameroon", "China", "Colombia", "Costa Rica",
        "Cuba", "Cape Verde", "Curacao", "Cyprus", "Czech Republic", "Germany", "Djibouti", "Denmark", "Dominica", "Dominican Republic", "Algeria", "Ecuador", "Estonia",
        "Egypt", "Eritrea", "Spain", "Ethiopia", "Finland", "Fiji", "Falkland (Malvinas) Islands", "Micronesia", "Faroe Islands", "France", "Gabon", "United Kingdom",
        "Grenada", "Georgia", "Ghana", "Gibraltar", "Greenland", "Gambia", "Guinea", "Equatorial Guinea", "Greece", "Guatemala", "Guam", "Guinea-Bissau", "Guyana",
        "Hong Kong", "Honduras", "Croatia", "Haiti", "Hungary", "Indonesia", "Ireland", "Israel", "India", "Iraq", "Iran", "Iceland", "Italy", "Jamaica", "Jordan",
        "Japan", "Kenya", "Kyrgyzstan", "Cambodia", "Kiribati", "Comoros", "Saint Kitts and Nevis", "North Korea", "South Korea", "Kuwait", "Cayman Islands", "Kazakhstan",
        "Laos", "Lebanon", "Saint Lucia", "Liechtenstein", "Sri Lanka", "Liberia", "Lesotho", "Lithuania", "Luxembourg", "Latvia", "Libya", "Morocco", "Monaco", "Moldova",
        "Montenegro", "Saint Martin", "Madagascar", "Marshall Islands", "Macedonia", "Mali", "Myanmar", "Mongolia", "Macau", "Northern Marianas", "Mauritania", "Montserrat",
        "Malta", "Mauritius", "Maldives", "Malawi", "Mexico", "Malaysia", "Mozambique", "Namibia", "New Caledonia", "Niger", "Nigeria", "Nicaragua", "Netherlands",
        "Norway", "Nepal", "Nauru", "Niue", "New Zealand", "Oman", "Panama", "Peru", "French Polynesia", "Papua New Guinea", "Philippines", "Pakistan", "Poland",
        "Saint Pierre and Miquelon", "Puerto Rico", "Palestine", "Portugal", "Palau", "Paraguay", "Qatar", "Reunion", "Romania", "Serbia", "Russia", "Rwanda", "Saudi Arabia",
        "Solomon Islands", "Seychelles", "Sudan", "Sweden", "Singapore", "Saint Helena", "Slovenia", "Slovakia", "Sierra Leone", "San Marino", "Senegal", "Somalia",
        "Suriname", "South Sudan", "Sao Tome and Principe", "El Salvador", "Sint Maarten", "Syria", "Swaziland", "Turks and Caicos Islands", "Chad", "Togo", "Thailand",
        "Tajikistan", "Tokelau", "East Timor", "Turkmenistan", "Tunisia", "Tonga", "Turkey", "Trinidad and Tobago", "Tuvalu", "Taiwan", "Tanzania", "Ukraine", "Uganda",
        "United States of America", "Uruguay", "Uzbekistan", "Vatican City", "Saint Vincent and the Grenadines", "Venezuela", "British Virgin Islands",
        "U.S. Virgin Islands", "Vietnam", "Vanuatu", "Wallis and Futuna", "Samoa", "Yemen", "Mayotte", "South Africa", "Zambia", "Zimbabwe"
    );

    public static final List<Integer> DIAL_CODES = Arrays.asList(
        376, 971, 93, 1, 1, 355, 374, 599, 244, 54, 1, 43, 61, 297, 994, 387, 1, 880, 32, 226, 359, 973, 257, 229, 590, 1, 673, 591, 55, 1, 975, 267, 375, 501, 1, 243,
        236, 41, 225, 682, 56, 237, 86, 57, 506, 53, 238, 599, 357, 420, 49, 253, 45, 1, 1, 213, 593, 372, 20, 291, 34, 251, 358, 679, 500, 691, 298, 33, 241, 44, 1,
        995, 233, 350, 299, 220, 224, 240, 30, 502, 1, 245, 592, 852, 504, 385, 509, 36, 62, 353, 972, 91, 964, 98, 354, 39, 1, 962, 81, 254, 996, 855, 686, 269, 1,
        850, 82, 965, 1, 7, 856, 961, 1, 423, 94, 231, 266, 370, 352, 371, 218, 212, 377, 373, 382, 590, 261, 692, 389, 223, 95, 976, 853, 1, 222, 1, 356, 230, 960,
        265, 52, 60, 258, 264, 687, 227, 234, 505, 31, 47, 977, 674, 683, 64, 968, 507, 51, 689, 675, 63, 92, 48, 508, 1, 970, 351, 680, 595, 974, 262, 40, 381, 7,
        250, 966, 677, 248, 249, 46, 65, 290, 386, 421, 232, 378, 221, 252, 597, 211, 239, 503, 1, 963, 268, 1, 235, 228, 66, 992, 690, 670, 993, 216, 676, 90, 1, 688,
        886, 255, 380, 256, 1, 598, 998, 379, 1, 58, 1, 1, 84, 678, 681, 685, 967, 262, 27, 260, 263
    );

    public static final List<Integer> SORT_ORDERS = Arrays.asList(
        50 ,2150 ,10 ,80 ,70 ,20 ,100 ,1410 ,60 ,90 ,40 ,130 ,120 ,110 ,140 ,260 ,180 ,170 ,200 ,320 ,310 ,160 ,330 ,220 ,1710 ,230 ,300 ,250 ,280 ,150 ,240 ,270 ,190
        ,210 ,360 ,520 ,390 ,1980 ,970 ,450 ,410 ,350 ,420 ,430 ,460 ,480 ,370 ,490 ,500 ,510 ,740 ,540 ,530 ,550 ,560 ,30 ,580 ,630 ,590 ,620 ,1920 ,640 ,680 ,670 ,650
        ,1280 ,660 ,690 ,710 ,2160 ,790 ,730 ,750 ,760 ,780 ,720 ,820 ,610 ,770 ,810 ,800 ,830 ,840 ,870 ,860 ,470 ,850 ,880 ,910 ,940 ,950 ,900 ,930 ,920 ,890 ,960 ,980
        ,1000 ,990 ,1020 ,1050 ,340 ,1030 ,440 ,1690 ,1480 ,1900 ,1040 ,380 ,1010 ,1060 ,1080 ,1700 ,1120 ,1930 ,1100 ,1090 ,1130 ,1140 ,1070 ,1110 ,1340 ,1300 ,1290 ,1320
        ,1720 ,1170 ,1230 ,1160 ,1210 ,1360 ,1310 ,1150 ,1490 ,1240 ,1330 ,1220 ,1250 ,1200 ,1180 ,1270 ,1190 ,1350 ,1370 ,1420 ,1450 ,1460 ,1440 ,1400 ,1500 ,1390 ,1380
        ,1470 ,1430 ,1510 ,1550 ,1580 ,700 ,1560 ,1590 ,1520 ,1600 ,1730 ,1620 ,1540 ,1610 ,1530 ,1570 ,1630 ,1640 ,1650 ,1800 ,1660 ,1670 ,1780 ,1870 ,1810 ,1940 ,1970 ,
        1830 ,1680 ,1860 ,1850 ,1820 ,1760 ,1790 ,1880 ,1950 ,1910 ,1770 ,600 ,1840 ,1990 ,1960 ,2110 ,400 ,2040 ,2030 ,2010 ,2050 ,570 ,2100 ,2080 ,2060 ,2090 ,2070 ,2120
        ,2000 ,2020 ,2140 ,2130 ,2170 ,2190 ,2200 ,2220 ,1740 ,2230 ,290 ,2180 ,2240 ,2210 ,2250 ,1750 ,2260 ,1260 ,1890 ,2270 ,2280
    );

    public static void main(String[] args)
    {
        for (int i=0; i<COUNTRY_CODES.size(); i++) {
//            String multilocalizationInsert = MessageFormat.format(INSERT_MULTILOCALIZATION_TEMPLATE, COUNTRY_CODES.get(i), COUNTRY_NAMES_EN.get(i));
//            System.out.println(multilocalizationInsert);

            String countryCodeInsert = MessageFormat.format(INSERT_COUNTRY_CODES_TEMPLATE, COUNTRY_CODES.get(i), DIAL_CODES.get(i), SORT_ORDERS.get(i));
            System.out.println(countryCodeInsert);
        }
    }

}
