package com.meinc.commons.encryption;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public interface IEncryption
{
	/**
	 * Encrypt a string using the SHA-256 hash algorithm.
	 * 
	 * @param plaintext
	 * @return the encrypted value
	 * @throws NoSuchAlgorithmException if SHA-256 isn't supported by this JVM
	 * @throws UnsupportedEncodingException if UTF-8 isn't supported by this JVM
	 */
	String oneWayEncrypt(String plaintext);
	
	/**
	 * Encrypt a string and store the value so it can be reversed later.
	 * 
	 * @param originalValue
	 * @param expiresDate The expiration date after which this encrypted value will no longer be valid
	 * @return
	 */
	String encryptValue(String namespace, String originalValue, Date expiresDate);
	
	/**
	 * Take an encrypted value and lookup the original value to reverse it.
	 * 
	 * @param encryptedValue
	 * @return
	 */
	String unencryptValue(String namespace, String encryptedValue);
	
	void deleteEncryptedValue(String namespace, String mungedValue);
	void deleteOriginalValue(String namespace, String origValue);
	  
	/**
	 * If you have an original value and want to know munged was generated for it, call this method.
	 * 
	 * @param originalValue
	 * @return
	 */
	String getMungedValueFromOriginalValue(String namespace, String originalValue);
	
	/**
	 * Use triple DES 168bit encryption to encrypt a value.  Neither the key nor the value are 
	 * stored anywhere.  It's all run-time in memory.
	 * 
	 * @param secretKeyInitializer the key initializer to use to encrypt the value.  In the form
	 * ###-###-###-###...
	 * @param plaintext the text to encrypt
	 * @return the encrypted text
	 */
	String symmetricalEncrypt(String secretKeyInitializer, String plaintext);
	
	/**
	 * Decrypt a value encrypted with the symmetricalEncrypt method.  
	 * 
	 * @param secretKeyInitializer the key initializer to use to decrypt the value.  In the form
	 * ###-###-###-###... Make sure the secretKeyInitializer is identical to the value used in the
	 * encryption.
	 * @param source the encrypted value
	 * @return the unencrypted value
	 */
	String symmetricalDecrypt(String secretKeyInitializer, String source);
	
	/**
	 * Generate a random string of a given length.
	 * @param length the length of the string to generate
	 * @return String the generated string
	 */
	String generateRandomString(int length);
	
	/**
	 * Generate a random 'fuzzybear27' password.  It will take a canned wordlist and combine 2 words, then add a number at the end.
	 * 
	 * Good for initial default passwords.
	 * 
	 * @return
	 */
	String generateRandomPassword();
	
    /**
     * Generate a random 'fuzzybear27' username.  It will take a canned wordlist and combine 2 words, then add a number at the end.
     * 
     * Good for initial usernames.  This uses a different wordlist than the random password method.
     * 
     * @return
     */
    String generateRandomUsername(String languageCode);
   
    /**
     * Take a string an run it through the scrypt algorithm: https://github.com/wg/scrypt
     * 
     * @param value
     * @return
     */
    String scryptEncode(String value);
   
    /**
     * Given a value and a hash, see if the value matches (for example, value might be a user entered password and hash might be the hashed value stored in the database)
     * 
     * @param value
     * @param hash
     * @return
     */
    boolean scryptCheck(String value, String hash);

    /**
     * Convenience method for {@link EncryptUtils#aes256Encode}. Using this service method will be slower due to service
     * method invocation overhead.
     * @see EncryptUtils#aes256Encode(byte[], String)
     */
    byte[] aes256Encode(byte[] data, String key);

    /**
     * Convenience method for {@link EncryptUtils#aes256Decode}. Using this service method will be slower due to service
     * method invocation overhead.
     * @see EncryptUtils#aes256Decode(byte[], String)
     */
    byte[] aes256Decode(byte[] data, String key);

    /**
     * Verify a password using the same scheme as the PASSWORD function available in MySQL 4.1+
     * @param password the plaintext password to verify
     * @param mysqlPasswordHash the encrypted hash produced by MySQL
     * @return true if the password matches the hash
     */
    boolean mysqlPasswordCheck(String password, String mysqlPasswordHash);

    /**
     * This will use the RSA/ESB/PKCS1Padding cipher.<p/>
     * To generate a public/private keypair using openssl and output them in DER format:
     * 
     * <pre>
     * openssl genrsa -out rsa.pem 2048
     * openssl rsa -in rsa.pem -pubout -outform DER -out public.der
     * openssl pkcs8 -topk8 -inform PEM -outform DER -in rsa.pem -out private.der -nocrypt
     * 
     * If you have a public PEM and need a public DER:
     * openssl rsa -pubin -inform PEM -in <filename of key in PEM format> -outform DER -out <filename of key in DER format>
     * 
     * To load a PublicKey from a DER file:
     * 
     * X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec({read the file into a byte[]});
     * PublicKey key = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
     * 
     * To load a PrivateKey from a DEF file:
     * 
     * PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec({read the file into a byte[]});
     * PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(pkcs8EncodedKeySpec);
     * </pre>
     * 
     * @param message The text to encrypt. This must be shorter than the number of bytes in the public key length.
     * @param encoding how is the input encoded (for example, UTF-8)
     * @param receiverPublicKey the openssl public key of the entity to whom you wish to pass this encrypted string 
     * @return a base64 encoded/encrypted string
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    String rsaEncrypt(String message, String encoding, PublicKey receiverPublicKey) 
    throws IOException, GeneralSecurityException;

    /**
     * This will use the RSA/ESB/PKCS1Padding cipher.<p/>
     * 
     * @param encryptedMessage A base64 encoded/encrypted string
     * @param encoding how is the input encoded (for example, UTF-8)
     * @param privateKey the openssl private key
     * @return the unencrypted message
     * @throws IOException
     * @throws GeneralSecurityException
     */
    String rsaDecrypt(String encryptedMessage, String encoding, PrivateKey privateKey)
    throws IOException, GeneralSecurityException;

    /**
     * This will use SHA256withRSA for the signature.
     * 
     * @param privateKey your private key 
     * @param dataToSign the message to sign
     * @param encoding how is the input encoded (for example, UTF-8)
     * @return a base64 encoded string that is the signature
     * @throws GeneralSecurityException
     * @throws IOException
     */
    String rsaSign(PrivateKey privateKey, String dataToSign, String encoding) 
    throws GeneralSecurityException, IOException;

    /**
     * This will use SHA256withRSA for the signature.
     * 
     * @param publicKey the public key of the entity that signed the message
     * @param originalData the unencrypted payload
     * @param signedData the signed/encrypted payload
     * @param encoding how is the originalData input encoded (for example, UTF-8)
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    boolean rsaVerifySignature(PublicKey publicKey, String originalData, String signedData, String encoding)
    throws GeneralSecurityException, IOException;

    /**
     * Use the AES/CBC/PCKS5PADDING cipher to do symmetrical encryption of a string.
     * 
     * @param message the text to encrypt
     * @param encoding how is the input encoded (for example, UTF-8)
     * @param passphrase must be 128 bits (16 bytes) 
     * @param initializationVector must be 16 bytes
     * @return a base64 encoded/encrypted string
     * @throws GeneralSecurityException
     * @throws IOException
     */
    String aesEncrypt(String message, String encoding, String passphrase, String initializationVector)
    throws GeneralSecurityException, IOException;

    /**
     * Use the AES/CBC/PKCS5PADDING cipher to do a symmetrical decryption of a string.
     * 
     * @param message the base64 encoded/encrypted string
     * @param encoding how should the output be encoded (for example, UTF-8)
     * @param passphrase must be 128 bits (16 bytes)
     * @param initializationVector must be 16 bytes
     * @return the decrypted string
     * @throws GeneralSecurityException
     * @throws IOException
     */
    String aesDecrypt(String message, String encoding, String passphrase, String initializationVector)
    throws GeneralSecurityException, IOException;

    /**
     * A convenience method to generate a random passphrase and initialization vector (using UUID's) for use in an AES symmetrical encryption.
     * The passphrase and initialization vector are themselves RSA encrypted using the given keys. All of the returned values can be safely 
     * passed to the recipient in clear text, where they can use their keys to decrypt the message.
     * 
     * @param message the unencrypted message
     * @param messageEncoding the encoding of the unencrypted message (such as UTF-8)
     * @param receiverPublicKey rsa public key of the message recipient
     * @param senderPrivateKey rsa private key of the message sender
     * @return All the relevant data from the encryption. This can be passed to the recipient. It is a String[] with the following values:
     * <ol start="0">
     *   <li>rsaEncryptedAesPassphrase</li>
     *   <li>rsaEncryptedAesInitializationVector</li>
     *   <li>aesMessageEncrypted</li>
     *   <li>rsaMessageSignature</li>
     * </ol>
     * @throws IOException
     * @throws GeneralSecurityException
     */
    String[] encryptAndSignMessage(String message, String messageEncoding, PublicKey receiverPublicKey, PrivateKey senderPrivateKey)
    throws IOException, GeneralSecurityException;

    /**
     * A convenience method to decrypt and verify the signature of an incoming message.
     * 
     * @param rsaEncryptedAesPassphrase
     * @param rsaEncryptedAesInitializationVector
     * @param aesEncryptedMessage
     * @param rsaMessageSignature
     * @param messageEncoding
     * @param receiverPrivateKey
     * @param senderPublicKey
     * @return all the relevant data from the decryption. It is an Object[] with the following values:
     * <ol start="0">
     *   <li>boolean - true if signature validation passed, false otherwise</li>
     *   <li>string - the unencrypted message. Only trust it if the signature is valid</li>
     * @throws IOException
     * @throws GeneralSecurityException
     */
    Object[] decryptAndVerifyMessage(
            String rsaEncryptedAesPassphrase, String rsaEncryptedAesInitializationVector, String aesEncryptedMessage, String rsaMessageSignature,
            String messageEncoding, PrivateKey receiverPrivateKey, PublicKey senderPublicKey)
    throws IOException, GeneralSecurityException;

}
