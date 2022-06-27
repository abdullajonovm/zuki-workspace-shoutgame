package com.meinc.commons.encryption;

import static org.springframework.transaction.annotation.Propagation.NESTED;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.lambdaworks.crypto.SCrypt;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;

@Service(
    name="EncryptionService",
    interfaces=EncryptionService.ENCRYPTION_INTERFACE,
    exposeAs=IEncryption.class)
public class EncryptionService
implements IEncryption
{
    public static final String ENCRYPTION_INTERFACE = "IEncryptionService";
    private static Logger _logger = Logger.getLogger(EncryptionService.class);

    private EncryptionDaoSqlMap _teamDao;
    private String[] _usernamePrefixList;
    private String[] _usernamePostfixList;
    private String[] _usernameEsList;
    private String[] _passwordPrefixList;
    private String[] _passwordPostfixList;

    public EncryptionService()
    {
        Properties p = new Properties();
        try {
            p.load(this.getClass().getResourceAsStream("/words.properties"));
            _usernamePrefixList = p.getProperty("username.prefix").split(",");
            _usernamePostfixList = p.getProperty("username.postfix").split(",");
            _usernameEsList = p.getProperty("username.es").split(",");
            _passwordPrefixList = p.getProperty("password.prefix").split(",");
            _passwordPostfixList = p.getProperty("password.postfix").split(",");
        } catch (IOException e) {
            _logger.error("unable to load words.properties file!", e);
        }
    }

    public EncryptionDaoSqlMap getTeamDao()
    {
        return _teamDao;
    }

    public void setTeamDao(EncryptionDaoSqlMap teamDao)
    {
        _teamDao = teamDao;
    }

    @Override
    @ServiceMethod
    public String oneWayEncrypt(String plaintext)
    {
        // java5 supports: MD2, MD5, SHA-1 (or SHA), SHA-256, SHA-384, and SHA-512
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // will not happen
            e.printStackTrace();
            return plaintext;
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // will not happen
            e.printStackTrace();
            return plaintext;
        }
        byte raw[] = md.digest();
        //String hash = (new BASE64Encoder()).encode(raw);
        String hash = new String(Base64.getEncoder().encode(raw));
        return hash;
    }

    @Override
    @ServiceMethod
    public String symmetricalEncrypt(String secretKeyInitializer, String plaintext)
    {
        Cipher desCipher;
        Key key;

        // get the secret key
        try {
            key = getSecretKey(secretKeyInitializer);
        } catch (Exception e) {
            // won't happen in properly configured system
            e.printStackTrace();
            return plaintext;
        }

        //initialize the cypher
        //* The following creates a DES Cipher that uses Electronic Codebook Mode (ECB) and PKCS5Padding
        //  * ECB: Electronic Codebook Mode, as defined in: The National Institute of Standards and Technology (NIST) Federal Information Processing Standard (FIPS) PUB 81, "DES Modes of Operation," U.S. Department of Commerce, Dec 1980.
        //    * PKCS5Padding: The padding scheme described in RSA Laboratories, "PKCS #5: Password-Based Encryption Standard," version 1.5, November 1993.
        try {
            // desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher = Cipher.getInstance("DESede");
            desCipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            // won't happen in properly configured system
            e.printStackTrace();
            return plaintext;
        }

        // encrypt the value
        byte[] cleartext = plaintext.getBytes();
        byte[] ciphertext;
        try {
            ciphertext = desCipher.doFinal(cleartext);
        } catch (Exception e) {
            // won't happen in properly configured system
            e.printStackTrace();
            return plaintext;
        }

        return getString(ciphertext);
    }

    @Override
    @ServiceMethod
    public String symmetricalDecrypt(String secretKeyInitializer, String source)
    {
        // http://www.informit.com/guides/content.asp?g=java&seqNum=31
        Cipher desCipher;
        Key key;

        // get the secret key
        try {
            key = getSecretKey(secretKeyInitializer);
        } catch (Exception e) {
            // won't happen in properly configured system
            e.printStackTrace();
            return source;
        }

        //initialize the cypher
        //* The following creates a DES Cipher that uses Electronic Codebook Mode (ECB) and PKCS5Padding
        //  * ECB: Electronic Codebook Mode, as defined in: The National Institute of Standards and Technology (NIST) Federal Information Processing Standard (FIPS) PUB 81, "DES Modes of Operation," U.S. Department of Commerce, Dec 1980.
        //    * PKCS5Padding: The padding scheme described in RSA Laboratories, "PKCS #5: Password-Based Encryption Standard," version 1.5, November 1993.
        try {
            // desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher = Cipher.getInstance("DESede");
            desCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            // won't happen in properly configured system
            e.printStackTrace();
            return source;
        }

        // encode the cyphertext as bytes, stripping out the "-" characters
        byte[] ciphertext = getBytes(source);

        // decrypt the ciphertext
        byte[] cleartext;
        try {
            cleartext = desCipher.doFinal(ciphertext);
        } catch (Exception e) {
            // oh well; nothing to do
            e.printStackTrace();
            return source;
        }

        return new String(cleartext);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String encryptValue(String namespace, String originalValue, Date expiresDate)
    {
        if (namespace == null || originalValue == null)
            return null;
        int hashCode = originalValue.hashCode();
        String mungedValue = getRandomNumberFromHashValue(hashCode);
        mungedValue = _teamDao.insertMungeValue(namespace, originalValue, mungedValue, expiresDate);
        return mungedValue;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String unencryptValue(String namespace, String encryptedValue)
    {
        if (namespace == null || encryptedValue == null)
            return null;
        String unmungedValue = _teamDao.getUnMungedValue(namespace, encryptedValue);
        return unmungedValue;
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void deleteEncryptedValue(String namespace, String mungedValue) {
        if (namespace == null || mungedValue == null)
            return;
        _teamDao.deleteMungedValue(namespace, mungedValue);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public void deleteOriginalValue(String namespace, String origValue) {
        if (namespace == null || origValue == null)
            return;
        _teamDao.deleteUnMungedValue(namespace, origValue);
    }

    @Override
    @ServiceMethod
    @Transactional(propagation=NESTED)
    public String getMungedValueFromOriginalValue(String namespace, String originalValue)
    {
        return _teamDao.getMungedValue(namespace, originalValue);
    }

    private String getRandomNumberFromHashValue(int hashValue)
    {
        String result = null;
        Random randObj = new Random(hashValue);
        String randomNum = Integer.toString(randObj.nextInt());
        int len = randomNum.length();

        randObj = new Random((int) System.currentTimeMillis());
        String randomStr = generateRandomString(randObj, len, len + 10);

        result = mergeNumberAndString(randomNum, randomStr);

        return result;
    }

    private String generateRandomString(Random randObj2, int lo, int hi)
    {
        Random randObj = new Random((int) System.currentTimeMillis());
        int n = rand(randObj, lo, hi);

        byte b[] = new byte[n];

        for (int i = 0; i < n; i++)
            b[i] = (byte) rand(randObj, 'a', 'z');

        return new String(b);
    }

    private int rand(Random randObj, int lowNum, int hiNum)
    {
        int n = hiNum - lowNum + 1;
        int i = randObj.nextInt() % n;
        if (i < 0)
            i = -i;

        return lowNum + i;
    }

    private String mergeNumberAndString(String randNum, String randStr)
    {
        int numSize = randNum.length();
        int strSize = randStr.length();
        int i;

        StringBuilder sb = new StringBuilder();

        if (numSize <= strSize) {
            for (i = 0; i < numSize; i++) {
                sb.append(randNum.charAt(i));
                sb.append(randStr.charAt(i));
            }

            if (i < (strSize - 1))
                sb.append(randStr.substring(i, strSize - 1));
        } else {
            for (i = 0; i < strSize; i++) {
                sb.append(randStr.charAt(i));
                sb.append(randNum.charAt(i));
            }

            if (i < (numSize - 1))
                sb.append(randNum.substring(i, strSize - 1));
        }

        return sb.toString();
    }

    /**
     * Generate the secret key used for the symmetrical encryption.
     *
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private Key getSecretKey(String secretKeyInitializer)
    throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] bytes = getBytes(secretKeyInitializer); // ensures we always get
                                                       // the same secret key
                                                       // generated for us
        KeyGenerator kgen = KeyGenerator.getInstance("DESede");
        SecureRandom sr = new SecureRandom(bytes);
        kgen.init(168, sr); // 112, 168
        SecretKey skey = kgen.generateKey();
        return skey;

        // DESKeySpec pass = new DESKeySpec(bytes);
        // SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        // SecretKey s = skf.generateSecret(pass);
        // return s;
    }

    /**
     * Convert a cypher byte[] into a string.
     *
     * @param ciphertext
     * @return
     */
    private String getString(byte[] ciphertext)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ciphertext.length; i++) {
            byte b = ciphertext[i];
            sb.append(0x00FF & b); // 0x00ff = 255
            if (i + 1 < ciphertext.length)
                sb.append("-");
        }

        return sb.toString();
    }

    /**
     * Take the input text and skip the "-" characters and convert the remaining
     * characters to a byte[]
     *
     * @param input
     * @return
     */
    private byte[] getBytes(String input)
    {
        int i;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringTokenizer st = new StringTokenizer(input, "-", false);

        while (st.hasMoreTokens()) {
            i = Integer.parseInt(st.nextToken());
            bos.write((byte) i);
        }

        return bos.toByteArray();
    }

    @Override
    @ServiceMethod
    public String generateRandomString(int length)
    {
        String ret_val = null;
        Random randObj = new Random((int) System.currentTimeMillis());
        byte[] chars = new byte[length];
        for (int i = 0; i < chars.length; i++) {
            int nextRange = randObj.nextInt() % 3;
            int nextVal = 0;
            switch (nextRange) {
            case 0:
                nextVal = rand(randObj, '0', '9');
                break;
            case 1:
                nextVal = rand(randObj, 'A', 'Z');
                break;
            case 2:
                nextVal = rand(randObj, 'a', 'z');
                break;
            default:
                nextVal = rand(randObj, 'a', 'z');
            }
            chars[i] = (byte) nextVal;
        }
        ret_val = new String(chars);
        return ret_val;
    }

    @Override
    @ServiceMethod
    public String generateRandomPassword()
    {
        Random r = new Random();
        String prefix = _passwordPrefixList[r.nextInt(_passwordPrefixList.length)];
        String postfix = _passwordPostfixList[r.nextInt(_passwordPostfixList.length)];
        String val = prefix + postfix + r.nextInt(100);
        val = val.replaceAll("[01]", "");
        return val;
    }

    @Override
    @ServiceMethod
    public String generateRandomUsername(String languageCode)
    {
        Random r = new Random();
        String val;

        if ("es".equals(languageCode)) {
            String prefix = _usernameEsList[r.nextInt(_usernameEsList.length)];
            String postfix = _usernameEsList[r.nextInt(_usernameEsList.length)];
            val = prefix + postfix;
        } else {
            String prefix = _usernamePrefixList[r.nextInt(_usernamePrefixList.length)];
            String postfix = _usernamePostfixList[r.nextInt(_usernamePostfixList.length)];
            val = prefix + postfix;
        }

        val = val + r.nextInt(100);
        val = val.replaceAll("[01]", ""); //0 and 1 look a lot like O and i and l
        return val;
    }

    @Override
    @ServiceMethod
    public String scryptEncode(String value)
    {
        try {
            byte[] salt = new byte[64];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);

            int N = 32768;
            int log2_N = 15;  //log2(32768) = 15
            int r = 8;
            int p = 1;

            byte[] hash = SCrypt.scrypt(value.getBytes("UTF-8"), salt, N, r, p, 64);

            String params = Long.toHexString(log2_N << 16L | r << 8 | p);

            StringBuilder result = new StringBuilder((salt.length + hash.length) * 2);
            result.append("$s0$").append(params).append('$');
            result.append(HexUtils.bytesToBase64String(salt)).append('$');
            result.append(HexUtils.bytesToBase64String(hash));

            return result.toString();
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    @ServiceMethod
    public boolean scryptCheck(String password, String scryptHash)
    {
        try {
            String[] parts = scryptHash.split("\\$");

            if (parts.length != 5 || !parts[1].equals("s0")) {
                throw new IllegalArgumentException("Invalid hashed value");
            }

            long params = Long.parseLong(parts[2], 16);
            byte[] salt = HexUtils.base64StringToBytes(parts[3]);
            byte[] scryptHashBytes = HexUtils.base64StringToBytes(parts[4]);

            int N = (int) params >> 16 & 0xffff;
            int r = (int) params >>  8 & 0xff;
            int p = (int) params       & 0xff;

            byte[] scryptRehashBytes = SCrypt.scrypt(password.getBytes("UTF-8"), salt, 1<<N, r, p, scryptHashBytes.length);

            return Arrays.equals(scryptHashBytes, scryptRehashBytes);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    @ServiceMethod
    public byte[] aes256Encode(byte[] data, String key) {
        return EncryptUtils.aes256Encode(data, key);
    }

    @Override
    @ServiceMethod
    public byte[] aes256Decode(byte[] data, String key) {
        return EncryptUtils.aes256Decode(data, key);
    }

    @Override
    @ServiceMethod
    public boolean mysqlPasswordCheck(String password, String mysqlPasswordHash)
    {
        // java5 supports: MD2, MD5, SHA-1 (or SHA), SHA-256, SHA-384, and SHA-512
        try {
            // MySQL uses a SHA1(2) hash
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(password.getBytes("UTF-8"));
            byte raw[] = md.digest();

            md.reset();
            md.update(raw);
            raw = md.digest();

            String hash = Hex.encodeHexString(raw);
            hash = "*" + hash.toUpperCase();
            System.out.println(hash);
            return hash.equals(mysqlPasswordHash);

        } catch (NoSuchAlgorithmException e) {
            // will not happen
            _logger.error(e.getMessage(), e);
            return false;
        } catch (UnsupportedEncodingException e) {
            // will not happen
            _logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    @ServiceMethod
    public String rsaEncrypt(String message, String encoding, PublicKey receiverPublicKey)
    throws IOException, GeneralSecurityException
    {
        /*
         *  <ul>
         *      <li>algorithm - symmetric or asymmetric - rsa is always asymmetric</li>
         *      <li>mode - RSA uses ECB (electronic codebook) - ignored by Java since ecb is only used for symmetric, and rsa is asymmetric</li>
         *      <li>padding - if padding is NOT used, rsa is susceptible to attacks, and should never be used (i.e. always use padding)</li>
         *  </ul>
         */
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);

        byte[] encryptedBytes = cipher.doFinal(message.getBytes(encoding));
        return new String(Base64.getEncoder().encode(encryptedBytes));
    }

    @Override
    @ServiceMethod
    public String rsaDecrypt(String encryptedMessage, String encoding, PrivateKey privateKey)
    throws IOException, GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] inBytes = Base64.getDecoder().decode(encryptedMessage.getBytes(encoding));
        return new String(cipher.doFinal(inBytes), encoding);
    }

    @Override
    @ServiceMethod
    public String rsaSign(PrivateKey privateKey, String dataToSign, String encoding)
    throws GeneralSecurityException, IOException
    {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(dataToSign.getBytes(encoding));
        byte[] signedData = sig.sign();

        return new String(Base64.getEncoder().encode(signedData));
    }

    @Override
    @ServiceMethod
    public boolean rsaVerifySignature(PublicKey publicKey, String originalData, String signedData, String encoding)
    throws GeneralSecurityException, IOException
    {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(originalData.getBytes(encoding));

        byte[] inBytes = Base64.getDecoder().decode(signedData.getBytes(encoding));
        return sig.verify(inBytes);
    }

    @Override
    @ServiceMethod
    public String aesEncrypt(String message, String encoding, String passphrase, String initializationVector)
    throws GeneralSecurityException, IOException
    {
        IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes(encoding));
        SecretKeySpec skeySpec = new SecretKeySpec(passphrase.getBytes(encoding), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return new String(Base64.getEncoder().encode(encryptedBytes));
    }

    @Override
    @ServiceMethod
    public String aesDecrypt(String message, String encoding, String passphrase, String initializationVector)
    throws GeneralSecurityException, IOException
    {
        IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes(encoding));
        SecretKeySpec skeySpec = new SecretKeySpec(passphrase.getBytes(encoding), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        //Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] inBytes = Base64.getDecoder().decode(message.getBytes(encoding));
        return new String(cipher.doFinal(inBytes), encoding);
    }

    @Override
    @ServiceMethod
    public String[] encryptAndSignMessage(String message, String messageEncoding, PublicKey receiverPublicKey, PrivateKey senderPrivateKey)
    throws IOException, GeneralSecurityException
    {
        //generate the passphrase and initialization vector needed by the symmetric AES encryption algorithm
        //the length of the passphrase and initialization vector must be a multiple of 16
        String aesPassphrase = UUID.randomUUID().toString().replace("-", "").substring(16);
        String aesInitializationVector = UUID.randomUUID().toString().replace("-", "").substring(0,16);

        //using the public key of the receiver, use asymmetric RSA encryption of the AES values so they can be securely transmitted
        String rsaEncryptedAesPassphrase = rsaEncrypt(aesPassphrase, messageEncoding, receiverPublicKey);
        String rsaEncryptedAesInitializationVector = rsaEncrypt(aesInitializationVector, messageEncoding, receiverPublicKey);

        //AES encrypt the message
        String aesEncryptedMessage = aesEncrypt(message, messageEncoding, aesPassphrase, aesInitializationVector);

        //RSA sign the message
        String rsaMessageSignature = rsaSign(senderPrivateKey, message, messageEncoding);

        return new String[] {
            rsaEncryptedAesPassphrase, rsaEncryptedAesInitializationVector, aesEncryptedMessage, rsaMessageSignature
        };
    }

    @Override
    @ServiceMethod
    public Object[] decryptAndVerifyMessage(
            String rsaEncryptedAesPassphrase, String rsaEncryptedAesInitializationVector, String aesEncryptedMessage, String rsaMessageSignature,
            String messageEncoding, PrivateKey receiverPrivateKey, PublicKey senderPublicKey)
    throws IOException, GeneralSecurityException
    {
        //use the RSA key to decrypt the AES passphrase and initializationVector
        String aesPassphrase = rsaDecrypt(rsaEncryptedAesPassphrase, messageEncoding, receiverPrivateKey);
        String aesInitializationVector = rsaDecrypt(rsaEncryptedAesInitializationVector, messageEncoding, receiverPrivateKey);

        //AES decrypt the message
        String message = aesDecrypt(aesEncryptedMessage, messageEncoding, aesPassphrase, aesInitializationVector);

        //RSA verify the message
        boolean isSignatureValid = rsaVerifySignature(senderPublicKey, message, rsaMessageSignature, messageEncoding);

        return new Object[] {isSignatureValid, message};
    }

    private static byte[] getFileAsBytes(String filename)
    throws IOException
    {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);
        dis.close();
        return keyBytes;
    }

    public static PublicKey getPublicKey(String filename)
    throws IOException, GeneralSecurityException
    {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(getFileAsBytes(filename));
        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
    }

    public static PrivateKey getPrivateKey(String filename)
    throws IOException, GeneralSecurityException
    {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(getFileAsBytes(filename));
        return KeyFactory.getInstance("RSA").generatePrivate(pkcs8EncodedKeySpec);
    }

/*
    public static void main(String[] args)
    throws Exception
    {
        EncryptionService crypto = new EncryptionService();

        String encoding = "UTF-8";
        String rsaKeyPath = "/Users/shawker/dev/_workspace-vote-dev/Java8Training/res/";
        PublicKey a_pubKey = getPublicKey(rsaKeyPath + "a_public.der");
        PrivateKey a_prvKey = getPrivateKey(rsaKeyPath + "a_private.der");
        PublicKey b_pubKey = getPublicKey(rsaKeyPath + "b_public.der");
        PrivateKey b_prvKey = getPrivateKey(rsaKeyPath + "b_private.der");

        //B -> A
        String[] encryptedAndSignedMessageData = crypto.encryptAndSignMessage(
                "The sun is shining. But the ice is slippery.",
                encoding,
                a_pubKey,
                b_prvKey);

        String rsaEncryptedAesPassphrase = encryptedAndSignedMessageData[0];
        String rsaEncryptedAesInitializationVector = encryptedAndSignedMessageData[1];
        String aesMessageEncrypted = encryptedAndSignedMessageData[2];
        String rsaMessageSignature = encryptedAndSignedMessageData[3];

        System.out.println("rsaEncryptedAesPassphrase: " + rsaEncryptedAesPassphrase);
        System.out.println("rsaEncryptedAesInitializationVector: " + rsaEncryptedAesInitializationVector);
        System.out.println("aesMessageEncrypted: " + aesMessageEncrypted);
        System.out.println("rsaMessageSignature: " + rsaMessageSignature);
        System.out.println("");

        //A <- B
        Object[] decryptedData = crypto.decryptAndVerifyMessage(rsaEncryptedAesPassphrase, rsaEncryptedAesInitializationVector, aesMessageEncrypted, rsaMessageSignature, encoding, a_prvKey, b_pubKey);
        if ((boolean)decryptedData[0]) {
            System.out.println((String)decryptedData[1]);
        } else {
            System.out.println("INVALID SIGNATURE!");
        }
    }
*/

//    public static void main(String[] args) throws IOException, GeneralSecurityException {
//        String message = "Hello World!";
//
//        PrivateKey privateKey = getPrivateKey("/Users/mpontius/Dev/workspaces/workspace.shout.game/DmStreamServerless/tmp/rsa.priv.pk8.der");
//        EncryptionService es = new EncryptionService();
//        String signed = es.rsaSign(privateKey, message, "UTF-8");
//        System.out.println(signed);
//
//        PublicKey publicKey = getPublicKey("/Users/mpontius/Dev/workspaces/workspace.shout.game/DmStreamServerless/tmp/rsa.pub.der");
//        boolean verified = es.rsaVerifySignature(publicKey, message, signed, "UTF-8");
//        System.out.println(verified);
//    }
}
