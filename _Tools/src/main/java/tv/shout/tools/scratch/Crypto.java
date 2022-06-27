package tv.shout.tools.scratch;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto
{
//    private String decode(String encodedMessage, String encryptKey)
//    {
//        byte[] base64EncodedAes256EncodedBytes;
//        try {
//            base64EncodedAes256EncodedBytes = encodedMessage.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e); //won't happen
//        }
//
//        byte[] aes256EncodedBytes = Base64.getDecoder().decode(base64EncodedAes256EncodedBytes);
//        byte[] plainTextBytes = EncryptUtils.aes256Decode(aes256EncodedBytes, encryptKey);
//        try {
//            return new String(plainTextBytes, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e); //won't happen
//        }
//    }
//
//    private String generateEncryptKey(int length)
//    {
//        byte[] randBytes = new byte[length];
//        Random rand = new Random(UUID.randomUUID().toString().hashCode());
//        for (int i = 0; i < randBytes.length; i++) {
//            if (i == randBytes.length / 2)
//                // Try to avoid random seed collisions
//                rand = new Random();
//            randBytes[i] = (byte) (rand.nextInt(94) + 33);
//        }
//        return new String(randBytes).replaceAll("\"", "_"); //if there is a " in the key it messes up the json serialization
//    }
//
//    private String encode(String plainTextMessage, String encryptKey)
//    {
//        byte[] plainTextBytes;
//        try {
//            plainTextBytes = plainTextMessage.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e); //won't happen
//        }
//        byte[] aes256EncodedBytes = EncryptUtils.aes256Encode(plainTextBytes, encryptKey);
//        byte[] base64Encodedaes256EncodedBytes = Base64.getEncoder().encode(aes256EncodedBytes);
//        try {
//            return new String(base64Encodedaes256EncodedBytes, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e); //won't happen
//        }
//    }

    public String encrypt(String key, String initVector, String value)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return new String(Base64.getEncoder().encode(encrypted), "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args)
    {
        Crypto crypto = new Crypto();

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String key = uuid.substring(0,16);
        String iv = uuid.substring(16);

//        String encryptedEncodedMessage = "23/IWKPTWXcydSTFFfUVtNAFTaz6SsQG2r+DR8A6T3IUpoV6q+hKXdPBMPetMN2nSTrWvA3sXrXmVQjihcqHodWgOnR8XjbhtBubVaJj0KGcuzPa/zBacuFotMHtzJS/smA4sNGyJbT+Q+Mr+Tqsi62cltfTqgIrf+O4lvoGBU3SWXFlwNFxxPGLWEoamUEDbmzwxk6o0GEzsJ3ptLSVgJlhHshxfqI+WkXTFNMjDf3WDjLhm/atLvXimGY1nirIW0D84rpXgoncDCZzADfN2fzhvdSDEfhhRD6WkYZbVtxVlqkweofm75L4KLhHgqMrurEjGGpoBz/GIZQ+8ObdU4/OAppclfkZSOzJDEMgxxyoIklOv1D6edDQkHIR+mTU0GYySwsn0LMtddN32Hss4g4WuyoplWTxw0wDF+L+7kRAIDEiS2A45oTO/hSUFhB6WSIaLrfC7U+XwUHvyRpHxr/5eHTlBS8TXRGYIcCKb4dB4g9zEMcngseTwTcvP9Fi7Q44aCKpXsCzuBzbMlMzkmNCsR01uSy2zX8OD9HBIQwh4siPb3uAfciDTABwexncjqQuXyCY156CGNCqiJj8YH7xTMfUh4lM48bcQKGMohG1kup8qL40Aer0a2Pf1Dymj603gfTB7q+GSzl03iGZfQLfu1K9TFcQ1c3z0DgWYXuEtDuC1uMX+WeNhfbYGT4PQy4RUHt6/1OiPYE/ozPPfpSqaVFc1ejcINe1nCtOqAWPHBDIEN91ImqKqTPpZmSjLFfbuOW2nGlvIbRqwJXR5kh9QHJOsg1dRDZtAKf4rzFUuyXn5lJbh14agkiBOX1aG1mV5Sis/Re3JfBbpiRqM83+jO44BjugSqwf/6fuapkUL/IIgkQRyYnQT1bSJWDp8Q6Y7Drw4YziVzOfJBNn4PB9lEd09LakSe9haL0hR9QuXzoBM56HjE+jaYxCEzu/B1q/GOUveFsO1CQ8Gy6kukV/ZnAL2BXTH/3VNnJjn6qsS5yPvnaE+Kgos9DT3KdiiNXM9pUXAv/y2no4inZh1z+hWyRaYDzyi0RlioaGO2JiPpx0yMn+b8iFby4fHzkS7C8e8mZr+VYUHiRsfiB+JFS+WzCnSA3UfOZvou3PbJVW+v7F9u/SQflugYWVKmlpBX3vmcFgnzoINZQl8somWCgehCzNYkMXMCgas2wMxOOVVyhg/rQf8k1iB832keMCxgNrl7oDmxy+D9NIJ9hN11mjRM65w3aSdVN0BUFdqH7rhvnElWFdMt9Zj573RCEiIFT0A6ndS9GU8Jh36D9Esq6YedtIbOnoL0CRQ2orZpwLEpJpk01SYvVFZdFMPz834G0/OhrOguNVVEFmuumjbqOEIgYvrm2vs44KHwVPXnDbOwxxNsK7f+C5BOqzMqiTp1YDDvz3sgKyy918oHxpPU+WHtSuF9HK8lVLPQ/VVOTCR2JfWvIlMR7JCAhnxdW+csLV+Lyjk7FFiwJbrBURWoAEFzDnC9jSoztDTTGvfl6hf/5hH2ep+L1iRjXXq9HKj+dMbVSepM2/3vDb5fJKbBXanvjIg85rBG0TzNEVSIabl1E5VjHJ9JGiOIU7wepqooAWe4fjfR96iMJUphALj4KaHGmMQGHEq4rZdWph+Eapozg5tc+hYFjvOO5C2mXSdiqQm2IYrZG9epbtbwLttxI8qaXhQTlpgqoyYS7k8m3250gXLlAcVBuHDOaghaAYfIAEccJQn+bUPXTdmu0/BwheUfPLRmdYdnEi6tR3Of5Ri8jUjQL5/MzjGb7X8YMzS6/veSy9vLId2awRPFChXg==";
//        String symmetricalKey = "\\44qpIv_V'rb&A:Y',E:;+dOkroc^_Sw";

        String originalMessage = "Where is the Suez Canal?";
//        String key = crypto.generateEncryptKey(32);

        System.out.println(" original: " + originalMessage);
        System.out.println("      key: " + key);

        String encryptedMessage = crypto.encrypt(key, iv, originalMessage);
        String decryptedMessage = crypto.decrypt(key, iv, encryptedMessage);

        System.out.println("encrypted: " + encryptedMessage);
        System.out.println("decrypted: " + decryptedMessage);
    }

}
