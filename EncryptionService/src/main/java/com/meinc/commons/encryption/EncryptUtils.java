package com.meinc.commons.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    /**
     * Encode data using AES-256 with CBC block cipher and PKCS7 padding. The key is hashed once using SHA-256. No salt
     * is used to harden the key. If the key is user-generated then you really should salt the key before using this
     * method or find another method that employs a salt.
     * 
     * @param data
     *            the source data to encrypt
     * @param key
     *            the encryption key
     * @return the encrypted bytes. the first 16 bytes are the initialization vector.
     */
    public static byte[] aes256Encode(byte[] data, String key) {
        Cipher cipher;
        try {
            // PKCS5Padding is interpreted as a synonym for PKCS7Padding in the cipher specification. It is simply a
            // historical artifact, and rather than change it Sun decided to simply pretend the PKCS5Padding means the
            // same as PKCS7Padding when applied to block ciphers with a blocksize greater than 8 bytes.
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        }
        byte[] keyHash = HexUtils.stringToSha256Bytes(key);
        SecretKeySpec keySpec = new SecretKeySpec(keyHash, "AES");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key: " + e.getMessage(), e);
        }
        byte[] encryptedData = null;
        try {
            encryptedData = cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e);
        }
        byte[] iv = cipher.getIV();
        byte[] result = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
        return result;
    }
    
    /**
     * Decode data using AES-256 with CBC block cipher and PKCS7 padding. The key is hashed once using SHA-256.
     * @param data the encrypted data to decrypt. the first 16 bytes must be the initialization vector used during encryption.
     * @param key the encryption key used during encryption
     * @return the decrypted bytes
     */
    public static byte[] aes256Decode(byte[] data, String key) {
        Cipher cipher;
        try {
            // PKCS5Padding is interpreted as a synonym for PKCS7Padding in the cipher specification. It is simply a
            // historical artifact, and rather than change it Sun decided to simply pretend the PKCS5Padding means the
            // same as PKCS7Padding when applied to block ciphers with a blocksize greater than 8 bytes.
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        }
        byte[] keyHash = HexUtils.stringToSha256Bytes(key);
        SecretKeySpec keySpec = new SecretKeySpec(keyHash, "AES");
        byte[] iv = new byte[16];
        byte[] encryptedData = new byte[data.length - iv.length];
        System.arraycopy(data, 0, iv, 0, iv.length);
        System.arraycopy(data, iv.length, encryptedData, 0, encryptedData.length);
        try {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
        byte[] decryptedData = null;
        try {
            decryptedData = cipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e);
        }
        return decryptedData;        
    }
}
