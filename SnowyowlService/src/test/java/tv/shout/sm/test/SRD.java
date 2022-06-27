package tv.shout.sm.test;

import tv.shout.sm.db.DbProvider;

/**
 * Retrieve and parse the SRD (service routing document). This is what will tell the networking layer
 * what servers are available and whhich WDS a given account should expect to publish/retrieve their
 * documents to/from.
 */
public class SRD
{
    private static SRD _instance;

//    private static final String SRD_URL_LOCAL = "http://static_dev.shoutgameplay.com:8080/srd2.json";
//    private static final String SRD_URL_DC4 = "http://dc4-static.shoutgameplay.com/srd2.json";

//    private static Logger _logger = Logger.getLogger(SRD.class);
//    private static AtomicBoolean _initializing = new AtomicBoolean(false);
    private DbProvider.DB _which;

//    /**
//     * The list of WebDataStore URL's to use for the given emailHash. The first entry is the primary URL and subsequent entries are fallbacks in case the primary doesn't work.
//     */
//    private String[] _wdsUrls;
//
//    /**
//     * The list of Collector URL's to use for the given emailHash. The first entry is the primary URL and subsequent entries are fallbacks in case the primary doesn't work.
//     */
//    private String[] _collectorUrls;

    public String[] getWdsUrls()
    {
        switch (_which)
        {
            case LOCAL:
                return new String[] {"snowl-wds-origin--0--dev1.shoutgameplay.com:8080"};

            case NC10_1:
                return new String[] {"snowl-wds-origin--0--nc10-1.shoutgameplay.com:443"};

            case NC11_1:
                return new String[] {"snowl-wds-origin--0--nc11-1.shoutgameplay.com:443"};

            default:
                throw new IllegalStateException("all cases are covered. this shouldn't happen");
        }
//        //< return _wdsUrls;
//        return new String[] { "snowl-wds-origin--0_dev1.shoutgameplay.com:8443" };
    }

    public String[] getCollectorUrls()
    {
        switch (_which)
        {
            case LOCAL:
                return new String[] {"snowl-collector--0--dev1.shoutgameplay.com:8080"};

            case NC10_1:
                return new String[] {"snowl-collector--0--nc10-1.shoutgameplay.com:443"};

            case NC11_1:
                return new String[] {"snowl-collector--0--nc11-1.shoutgameplay.com:443"};

            default:
                throw new IllegalStateException("all cases are covered. this shouldn't happen");
        }

//        //< return _collectorUrls;
//        return new String[] { "snowl-collector--0_dev1.shoutgameplay.com:8443" };
    }

    public String[] getMediaUrls()
    {
        switch (_which)
        {
            case LOCAL:
                return new String[] {"snowl-wms-origin--0--dev1.shoutgameplay.com:8080"};

            case NC10_1:
                return new String[] {"snowl-wms-origin--0--nc10-1.shoutgameplay.com:443"};

            case NC11_1:
                return new String[] {"snowl-wms-origin--0--nc11-1.shoutgameplay.com:443"};

            default:
                throw new IllegalStateException("all cases are covered. this shouldn't happen");
        }

//        //we do NOT want https for media files
//        return new String[] { "snowl-wms-origin--0_dev1.shoutgameplay.com:8080" };
    }

    public static SRD getInstance(DbProvider.DB which, String email)
    {
        if (_instance == null) {
            initialize(which, email);
            return null;
//        } else if (_initializing.get()) {
//            return null;
        } else {
            return _instance;
        }
    }

    /**
     * This should be called early on in the app startup process so that initialization is complete before any network calls need to happen.
     *
     * @param email the email of the logged in user. This is to help the system determine which Collector and WDS to use when accessing
     * the server. If this value is null, it will default to the first WDS/Collector returned by the SRD. In which case, once the email is known,
     * this should be called again so that the proper value can be calculated on subsequent calls.
     */
    public static void initialize(DbProvider.DB which, String email)
    {
        _instance = new SRD();
        _instance._which = which;

//        String srdUrl;
//        switch (which)
//        {
//            case LOCAL:
//                srdUrl = SRD_URL_LOCAL;
//                break;
//
//            case DC4:
//                srdUrl = SRD_URL_DC4;
//                break;
//
//                x
//
//            default:
//                throw new IllegalArgumentException("unsupported server");
//        }
//
//        //make sure initialization isn't already occurring
//        if (! _initializing.compareAndSet(false, true)) {
//            _logger.debug("ignoring SRD initialization; initialization already in progress");
//            return;
//        } else {
//            _logger.debug("initializing SRD from: " + srdUrl);
//        }
//
////        //fetch the srd from the network
//        String srdJsonStr;
////        try {
////            srdJsonStr = HttpLibrary.httpGet(srdUrl);
////        } catch (IOException | NetworkException e) {
////            _logger.error("unable to retrieve the SRD", e);
////            _initializing.set(false);
////            return;
////        }
//        srdJsonStr = "{\"docType\":\"srd/2.0\",\"data\":{\"virtualStadium\":{\"SHOUT5\":{\"server\":\"wds\",\"path\":\"/VS/SHOUT5.json\"},\"MyMadrid\":{\"server\":\"wds\",\"path\":\"/VS/MyMadrid.json\"},\"SHOUT\":{\"server\":\"wds\",\"path\":\"/VS/SHOUT.json\"}}},\"server\":{\"wds\": {\"domainNameSets\":[[\"dc4-wds1.shoutgameplay.com:41801\",\"dc4-wds1-red1.shoutgameplay.com:41801\"],[\"dc4-wds2.shoutgameplay.com:41802\",\"dc4-wds2-red1.shoutgameplay.com:41802\"]],\"selectMethod\":\"sha256%4\"},\"collector\": {\"domainNameSets\":[[\"dc4-collector1.shoutgameplay.com:43441\",\"dc4-collector1-red1.shoutgameplay.com:43441\"]],\"selectMethod\":\"sha256%4\"},\"auth\": {\"domainNameSets\":[[\"dc4-auth1.shoutgameplay.com:44441\",\"dc4-auth1-red1.shoutgameplay.com:44441\"]],\"selectMethod\":\"sha256%4\"},\"static\": {\"domainNameSets\":[[\"dc4-static.shoutgameplay.com\"]],\"selectMethod\":\"sha256%4\"},\"sync\": {\"domainNameSets\":[[\"dc4-sync1.shoutgameplay.com:45441\",\"dc4-sync1-red1.shoutgameplay.com:45441\"],[\"dc4-sync2.shoutgameplay.com:45442\",\"dc4-sync2-red1.shoutgameplay.com:45442\"]],\"selectMethod\":\"sha256%4\"},\"vscollect\": {\"domainNameSets\":[[\"dc4-collector1.shoutgameplay.com:43441\",\"dc4-collector1-red1.shoutgameplay.com:43441\"],[\"dc4-collector2.shoutgameplay.com:43442\",\"dc4-collector2-red1.shoutgameplay.com:43442\"]],\"selectMethod\":\"sha256%4\"}},\"action\":{\"noseat\":\"vscollect\",\"vs\":\"static\",\"answer\":\"collector\",\"signin\":\"auth\",\"notify\":\"collector\",\"publish\":\"collector\",\"wds\":\"wds\",\"signup\":\"auth\",\"default\":\"sync\"}}";
//
//        //convert to json
//        JsonNode srdJson;
//        try {
//            srdJson = new ObjectMapper().readTree(srdJsonStr);
//        } catch (IOException e) {
//            _logger.error("unable to parse the SRD", e);
//            _initializing.set(false);
//            return;
//        }
//
//        //parse
//        if (!srdJson.has("docType")) {
//            _logger.error("SRD document has no docType attribute; unable to parse");
//            _initializing.set(false);
//            return;
//        }
//        String docType = srdJson.get("docType").asText();
//        switch (docType)
//        {
//            case "srd/2.0":
//                parseSrd20(srdJson, email);
//                break;
//
//            default:
//                _logger.error("unsupported SRD docType: " + docType);
//                _initializing.set(false);
//                return;
//        }
    }

//    private static void parseSrd20(JsonNode srdJson, String email)
//    {
//        SRD srd = new SRD();
//        try {
//            srd._wdsUrls = getV2ServerUrlsByType(srdJson, "wds", email);
//            srd._collectorUrls = getV2ServerUrlsByType(srdJson, "collector", email);
//
//            _logger.debug("SRD initialization complete");
//            _instance = srd;
//
//        } catch (IllegalStateException e) {
//            _logger.error(e.getMessage());
//        }
//
//        _initializing.set(false);
//    }

//    private static String[] getV2ServerUrlsByType(JsonNode srdJson, String type, String email)
//    {
//        JsonNode serverJson = srdJson.get("server");
//        JsonNode serverTypeJson = serverJson.get(type);
//
//        //determine which algorithm to use with the email hash to determine which server to use
//        String selectMethod = serverTypeJson.get("selectMethod").asText();
//        if (selectMethod.startsWith("sha256")) {
//            //find which entry in the list to use
//            int howManyBytesToUse = Integer.parseInt(selectMethod.split("%")[1]);
//            ArrayNode domainNameSetsListJson = (ArrayNode) serverTypeJson.get("domainNameSets");
////            int DS = domainNameSetsListJson.size();
//            int DS = 1; //TOxDO: for testing, only going to the first set (i.e. collector1)
//            int domainNameSetIndex;
//            if (email != null) {
//                byte[] emailHexBytes = HexUtils.stringToSha256Bytes(email);
//                long BV = HexUtils.getByteValueFromAlreadyHashedValue(emailHexBytes, howManyBytesToUse);
//                domainNameSetIndex = (int) (BV % DS);
//            } else {
//                domainNameSetIndex = 0; //no email hash given yet. default to the first entry in the list
//            }
//
//            //loop the inner array and pull out the list of endpoints
//            ArrayNode serverTypeListJson = (ArrayNode) domainNameSetsListJson.get(domainNameSetIndex);
//            String[] serverUrlsByType = new String[serverTypeListJson.size()];
//            for (int i=0; i<serverTypeListJson.size(); i++) {
//                serverUrlsByType[i] = serverTypeListJson.get(i).asText();
//            }
//
//            return serverUrlsByType;
//
//        } else {
//            throw new IllegalStateException(MessageFormat.format("unable to parse SRD: unsupported selectMethod: {0} for type: {1}", selectMethod, type));
//        }
//    }
}
