package tv.shout.sm.test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meinc.commons.encryption.EncryptionService;
import com.meinc.commons.encryption.HexUtils;
import com.meinc.identity.domain.Subscriber;

import tv.shout.sm.db.DbProvider;
import tv.shout.util.FastMap;
import tv.shout.util.JsonUtil;

public class AddSponsorSubscribers
{
    private static final String SUPERUSER_EMAIL = "shawker@me-inc.com";
    private static final DbProvider.DB SERVER = DbProvider.DB.NC11_1;

    private static final String NAME_LIST_MALE = "/Users/shawker/Downloads/male-names.txt";
    private static final String NAME_LIST_FEMALE = "/Users/shawker/Downloads/female-names.txt";
    private static final String DIR_PHOTO_MALE = "/Users/shawker/Downloads/profile-pics/malephotos";
    private static final String DIR_PHOTO_FEMALE = "/Users/shawker/Downloads/profile-pics/femalephotos";

    private DbProvider _db;

    private String _mediaUri;
    private String _collectorUri;
    private String _wdsUrl;
    private String _toWds;

    public AddSponsorSubscribers()
    throws KeyManagementException, NoSuchAlgorithmException
    {
        CertificateManager.trustAllCertificates();
        SRD.getInstance(SERVER, SUPERUSER_EMAIL); //causes initialization
        SRD srd = SRD.getInstance(SERVER, SUPERUSER_EMAIL); //returns the initialized object

        _db = new DbProvider(SERVER);

        _mediaUri = "https://" + srd.getMediaUrls()[0];
        _collectorUri = "https://" + srd.getCollectorUrls()[0];
        _toWds = srd.getWdsUrls()[0].split(":")[0];
        _wdsUrl =  "https://" + srd.getWdsUrls()[0];
    }

    public void addSponsorSubscribers()
    throws Exception
    {
        List<String> maleNames = Files.readAllLines(new File(NAME_LIST_MALE).toPath());
        List<File> malePhotos =
                Arrays.asList(new File(DIR_PHOTO_MALE).listFiles()).stream()
                .filter(f -> f.getName().endsWith("jpg")).collect(Collectors.toList());

        List<String> femaleNames = Files.readAllLines(new File(NAME_LIST_FEMALE).toPath());
        List<File> femalePhotos =
                Arrays.asList(new File(DIR_PHOTO_FEMALE).listFiles()).stream()
                .filter(f -> f.getName().endsWith("jpg")).collect(Collectors.toList());

        System.out.println("MALE SPONSORS ("+maleNames.size()+")");
        for (int i=0; i<maleNames.size(); i++) {
            String nickname = maleNames.get(i);
            String photoFilename = malePhotos.size() > i ? malePhotos.get(i).getAbsolutePath() : null;
            String photoUrl = photoFilename == null ? null : uploadFile(photoFilename);

            System.out.println(MessageFormat.format("creating subscriber {0}/{1}: {2} with photo {3}}", i, maleNames.size(), nickname, photoFilename));
            createSubscriber(nickname, photoUrl, photoFilename);
        }

        System.out.println("FEMALE SPONSORS ("+femaleNames.size()+")");
        for (int i=0; i<femaleNames.size(); i++) {
            String nickname = femaleNames.get(i);
            String photoFilename = femalePhotos.size() > i ? femalePhotos.get(i).getAbsolutePath() : null;
            String photoUrl = photoFilename == null ? null : uploadFile(photoFilename);

            System.out.println(MessageFormat.format("creating subscriber {0}/{1}: {2} with photo {3}", i, femaleNames.size(), nickname, photoFilename));
            createSubscriber(nickname, photoUrl, photoFilename);
        }
    }

    private String uploadFile(String filename)
    throws IOException, NetworkException
    {
        File file = new File(filename);
        if (!file.exists()) throw new IllegalArgumentException("file not found: " + file.getAbsolutePath());
        byte[] data = Files.readAllBytes(file.toPath());

        String extension = getFileExtension(file);
        String uuid = UUID.randomUUID().toString();

        //upload the file
        String postUrl = _mediaUri + "/" + uuid + "." + extension;
        String attachmentName = uuid;
        String attachmentFileName = uuid + "." + extension;

//        System.out.println(MessageFormat.format(
//            "postUrl: {0}\nattachmentFileName: {1}\ndata: {2}\nextension: {3}",
//            postUrl, attachmentFileName, data.length, extension));

        //perform the upload
        HttpLibrary.httpMultipartPost(postUrl, attachmentName, attachmentFileName, data);

        return postUrl;
    }

    private String getFileExtension(File file)
    {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf+1);
    }

    private void createSubscriber(String nickname, String photoUrl, String photoFilename)
    throws Exception
    {
        Map<String, String> params = new FastMap<>(
            "toWds", _toWds,
            "appId", "snowyowl",
            "firstName", "NYI",
            "lastName", "NYI",
            "email", nickname + "@test.com",
            "nickname", nickname,
            "birthDate", "1990-01-01T00:00:00.000",
            "isAdult", "true",
            "region", "UT",
            "password", new EncryptionService().scryptEncode(HexUtils.stringToSha256HexString("shout123", true)),
            "languageCode", "en"
        );

        if (photoUrl != null) {
            params.put("photoUrl", photoUrl);
        }

        Map<String, String> headers = new FastMap<>(
            "X-REST-DEVICE-ID", UUID.randomUUID().toString(),
            "X-REST-APPLICATION-ID", "snowyowl",
            "X-REST-APPLICATION-VERSION", "1.0",
            "deviceModel", "internal",
            "deviceName", "testapi",
            "deviceVersion", "1.0",
            "deviceOsName", "java",
            "deviceOsType", "Mac OS"
        );

        String httpResponse = HttpLibrary.httpPost(_collectorUri + "/auth/signup", headers, params);
        //System.out.println("/auth/signup RESPONSE: " + httpResponse);

        CollectorResponse response = CollectorResponse.fromJsonString(httpResponse);
        Thread.sleep(response.estimatedWaitTime*2);

        String getUrl = _wdsUrl + "/" + response.ticket + "/response.json";
        //System.out.println("grabbing response: " + getUrl);
        String wdsResponse = null;
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                wdsResponse = HttpLibrary.httpGet(getUrl);
                break;

            } catch (NetworkException e) {
                //it's possible things slowed down; if it's a 404, give it more time
                if (retryCount > 0 && e.httpResponseCode == 404) {
                    retryCount--;
                    Thread.sleep(response.estimatedWaitTime*2);
                }
            }
        }
        //System.out.println("encrypted response RESPONSE: " + wdsResponse);

        AuthLoginWdsEncryptedResponse subResponse = AuthLoginWdsEncryptedResponse.fromJsonString(wdsResponse);
        if (subResponse.nicknameInvalid) {
            //try another nickname
            String newNickname = getNextNickname(nickname);
            System.out.println("invalid nickname, trying again with: " + newNickname);
            if (photoFilename != null) {
                photoUrl = uploadFile(photoFilename);
            }
            createSubscriber(newNickname, photoUrl, photoFilename);
            return;
        }
        subResponse.decrypt(response.encryptKey);
        //System.out.println("Subscriber: " + subResponse.sub);

        addSubscriberAsSponsorPlayer(subResponse.sub.getSubscriberId());
    }

    private void addSubscriberAsSponsorPlayer(long subscriberId)
    throws Exception
    {
        String sql = "insert into snowyowl.sponsor_player (subscriber_id) values (?)";

        Connection con = _db.getConnection();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setLong(1, subscriberId);
            ps.executeUpdate();

            System.out.println("added sponsorPlayer: " + subscriberId);

        } finally {
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (con != null) {
                con.close();
                con = null;
            }
        }
    }

    private static class AuthLoginWdsEncryptedResponse
    {
        public boolean success;
        public boolean nicknameInvalid;
        public boolean unexpectedError;
        public String message;
        public String sessionKey;
        public String subscriber; //encrypted
        public Subscriber sub; //will be populated after decrypt is called

        public static AuthLoginWdsEncryptedResponse fromJsonString(String jsonResponse)
        throws IOException
        {
            //a convenient way to convert the JSON string into a Java object of a specific type by matching up field names
            jsonResponse = jsonResponse.replace("\\", "\\\\");
            ObjectMapper mapper = JsonUtil.getObjectMapper();
            AuthLoginWdsEncryptedResponse cr = mapper.readValue(jsonResponse, new TypeReference<AuthLoginWdsEncryptedResponse>() {});
            return cr;
        }

        public void decrypt(String encryptKey)
        throws Exception
        {
            String key = encryptKey.substring(0,16);
            String initVector = encryptKey.substring(16);

            byte[] decodedEncryptedBytes;
            try {
                decodedEncryptedBytes = Base64.getDecoder().decode(subscriber.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException impossible) {
                //if utf-8 isn't supported, there are bigger problems to deal with
                throw new IllegalStateException(impossible);
            }

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(decodedEncryptedBytes);
            String subscriberString = new String(original);

            subscriberString = subscriberString.replace("\\", "\\\\");
            ObjectMapper mapper = JsonUtil.getObjectMapper();
            sub = mapper.readValue(subscriberString, new TypeReference<Subscriber>() {});
        }

    }

    private String getNextNickname(String nickname)
    {
        boolean endsInNumber = Character.isDigit(nickname.charAt(nickname.length()-1));

        int nextNumber;
        String prefix;
        if (!endsInNumber) {
            prefix = nickname;
            nextNumber = 0;

        } else {
            String[] part = nickname.split("(?<=\\D)(?=\\d)");
            StringBuilder buf = new StringBuilder();
            for (int i=0; i<part.length-1; i++) {
                buf.append(part[i]);
            }

            prefix = buf.toString();
            nextNumber = Integer.parseInt(part[part.length-1])+1;
        }

        return prefix + nextNumber;
    }

    public static void main(String[] args)
    {
        try {
            AddSponsorSubscribers instance = new AddSponsorSubscribers();
            instance.addSponsorSubscribers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args)
//    {
//        List<String> nicknames = Arrays.asList(
//            "john", "john1", "john1iscool", "john1iscool1"
//        );
//
//        for (String nickname : nicknames) {
//            String newNickname = getNextNickname(nickname);
//            System.out.println(nickname + " -> " + newNickname);
//        }
//    }

}

