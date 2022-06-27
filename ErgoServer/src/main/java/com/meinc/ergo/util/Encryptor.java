package com.meinc.ergo.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;
import com.lambdaworks.crypto.SCryptUtil;

//http://www.exampledepot.com/egs/javax.crypto/passkey.html - nogo; uses DES
//http://www.code2learn.com/2011/06/encryption-and-decryption-of-data-using.html
public class Encryptor
{
    private static final Log logger = LogFactory.getLog(Encryptor.class);
    private static final String ALGORITHM = "AES";
    
    private String passKey;

    public Encryptor(String passKey) {
        this.passKey = passKey;
    }

    public String encrypt(String str)
    {
        String encryptedHash = SCryptUtil.scrypt(passKey, 16384, 8, 1);
        
        String[] encryptedHashParts = encryptedHash.split("\\$");
        if (encryptedHashParts.length != 5 || !"s0".equals(encryptedHashParts[1]))
            throw new IllegalStateException("Unexpected scrypt return value: " + encryptedHash);
        
        String scryptParms = encryptedHashParts[2];
        String scryptSalt = encryptedHashParts[3];
        String scryptKey = encryptedHashParts[4];
        byte[] scrypteKeyBytes = Base64.decode(scryptKey.toCharArray());
        
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // Shouldn't ever happen
            logger.error("Internal error: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (NoSuchPaddingException e) {
            // Shouldn't ever happen
            logger.error("Internal error: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        
        SecretKeySpec key = new SecretKeySpec(scrypteKeyBytes, ALGORITHM);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        
        byte[] encodedBytes;
        try {
            encodedBytes = cipher.doFinal(str.getBytes("UTF-8"));
        } catch (IllegalBlockSizeException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (BadPaddingException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (UnsupportedEncodingException e) {
            // Shouldn't ever happen
            logger.error("Internal error: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        String encodedString = new String(Base64.encode(encodedBytes));
        
        StringBuffer encodedBuffer = new StringBuffer();
        encodedBuffer.append("$s0a$");
        encodedBuffer.append(scryptParms);
        encodedBuffer.append("$");
        encodedBuffer.append(scryptSalt);
        encodedBuffer.append("$");
        encodedBuffer.append(encodedString);
        
        return encodedBuffer.toString();
    }
    
    @SuppressWarnings("restriction")
    public String decrypt(String encryptedString) 
    {
        byte[] keyBytes = null;
        byte[] encryptedBytes = null;

        String[] encryptedStringParts = encryptedString.split("\\$");
        if (encryptedStringParts.length == 1) {
            // Old weak cipher
            keyBytes = passKey.getBytes();
            try {
                encryptedBytes = new sun.misc.BASE64Decoder().decodeBuffer(encryptedString);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        } else if (encryptedStringParts.length == 5) {
            // New strong cipher
            if (!"s0a".equals(encryptedStringParts[1]))
                throw new IllegalArgumentException("Unknown encryption of type '"+encryptedStringParts[1]+"'");
            
            long scryptParams = Long.parseLong(encryptedStringParts[2], 16);
            byte[] saltBytes = Base64.decode(encryptedStringParts[3].toCharArray());
            encryptedBytes = Base64.decode(encryptedStringParts[4].toCharArray());

            int N = (int) Math.pow(2, scryptParams >> 16 & 0xffff);
            int r = (int) scryptParams >> 8 & 0xff;
            int p = (int) scryptParams & 0xff;

            try {
                keyBytes = SCrypt.scrypt(passKey.getBytes("UTF-8"), saltBytes, N, r, p, 32);
            } catch (UnsupportedEncodingException e) {
                // Shouldn't ever happen
                logger.error("Internal error: " + e.getMessage(), e);
                throw new IllegalStateException(e);
            } catch (GeneralSecurityException e) {
                // Shouldn't ever happen
                logger.error("Internal error: " + e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalArgumentException("Unknown encryption of type '"+encryptedStringParts[1]+"'");
        }

        SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // Shouldn't ever happen
            logger.error("Internal error: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (NoSuchPaddingException e) {
            // Shouldn't ever happen
            logger.error("Internal error: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        byte[] decValue;
        try {
            decValue = cipher.doFinal(encryptedBytes);
        } catch (IllegalBlockSizeException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (BadPaddingException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        String decryptedValue;
        try {
            decryptedValue = new String(decValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Shouldn't ever happen
            logger.error("Internal error: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        return decryptedValue;
    }
}
