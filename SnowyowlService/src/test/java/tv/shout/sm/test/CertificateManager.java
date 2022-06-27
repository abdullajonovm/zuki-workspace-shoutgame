package tv.shout.sm.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.xml.bind.DatatypeConverter;

public class CertificateManager
{

    public static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException
    {
//https://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//
//        TrustManager tm = new X509TrustManager() {
//            @Override
//            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            }
//
//            @Override
//            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            }
//
//            @Override
//            public X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//        };
//
//        sslContext.init(null, new TrustManager[] { tm }, null);

        //https://log.rowanto.com/java-8-turning-off-ssl-certificate-check/
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                    return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

    //https://stackoverflow.com/questions/12501117/programmatically-obtain-keystore-from-pem
    public static void addTrustedCertificate(String crtFilename, String pemFilename)
    throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException
    {
        SSLContext context = SSLContext.getInstance("TLS");

        byte[] crtBytes = Files.readAllBytes(Paths.get(crtFilename));
        byte[] pemBytes = Files.readAllBytes(Paths.get(pemFilename));

        byte[] certBytes = parseDERFromPEM(crtBytes, "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
        byte[] keyBytes = parseDERFromPEM(pemBytes, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

        X509Certificate cert = generateCertificateFromDER(certBytes);
        RSAPrivateKey key  = generatePrivateKeyFromDER(keyBytes);

        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);
        keystore.setCertificateEntry("cert-alias", cert);
        keystore.setKeyEntry("key-alias", key, "changeit".toCharArray(), new Certificate[] {cert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keystore, "changeit".toCharArray());

        KeyManager[] km = kmf.getKeyManagers();
        context.init(km, null, null);
    }

    private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter)
    {
        String data = new String(pem);
        String[] tokens = data.split(beginDelimiter);
        tokens = tokens[1].split(endDelimiter);
        return DatatypeConverter.parseBase64Binary(tokens[0]);
    }

    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes)
    throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes)
    throws CertificateException
    {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
