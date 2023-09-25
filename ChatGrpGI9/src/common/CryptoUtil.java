package common;

import application.Packet;

import javax.crypto.Mac;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

public class CryptoUtil {
    public String publicKeyFromCertificate(String fileName) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(fileInputStream);

        PublicKey publicKey = certificate.getPublicKey();
        byte[] publicKeyBytes = publicKey.getEncoded();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

        return publicKeyBase64.substring(0, 16);
    }
    public PublicKey publicKeyFromCertificateRSA(String fileName) throws Exception {
        FileInputStream fileInputStream=new FileInputStream(fileName);
        CertificateFactory certificateFactory=CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(fileInputStream);
        return certificate.getPublicKey();
    }
    public PrivateKey privateKeyFromJKS(String fileName, String jksPassWord, String alias) throws Exception {
        FileInputStream fileInputStream=new FileInputStream(fileName);
        KeyStore keyStore=KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(fileInputStream,jksPassWord.toCharArray());
        Key key = keyStore.getKey(alias, jksPassWord.toCharArray());
        PrivateKey privateKey= (PrivateKey) key;
        return privateKey;
    }

    public String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public byte[] decodeFromBase64(String dataBase64) {
        return Base64.getDecoder().decode(dataBase64.getBytes());
    }
    //HMAC signature and verification methods below
    public String hmacSign(byte[] data,String privateSecret) throws Exception {
        SecretKeySpec secretKeySpec=new SecretKeySpec(privateSecret.getBytes(),"HmacSHA256");
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(data);
        return Base64.getEncoder().encodeToString(signature);
    }
    public boolean hmacVerify(SealedObject sealedObject, String secret) throws Exception {
        Packet packet= Cryptography.decryptSignature(sealedObject);
        SecretKeySpec secretKeySpec=new SecretKeySpec(secret.getBytes(),"HmacSHA256");
        Mac mac=Mac.getInstance("HmacSHA256");
        String signature=packet.getSignature();
        packet.setSignature(null);
        mac.init(secretKeySpec);
        byte[] sign = mac.doFinal(packet.getDocumentSignature().getBytes());
        String base64Sign=Base64.getEncoder().encodeToString(sign);
        return (base64Sign.equals(signature));
    }
    //RSA signature and verification methods below
    public String rsaSign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature= Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey,new SecureRandom());
        signature.update(data);
        byte[] sign = signature.sign();
        return Base64.getEncoder().encodeToString(sign);
    }

    public boolean rsaSignVerify(SealedObject sealedObject,PublicKey publicKey) throws Exception {
        Signature signature=Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        Packet packet= Cryptography.decryptSignature(sealedObject);
        String sign=packet.getSignature();
        byte[] decodeSignature = Base64.getDecoder().decode(sign);
        signature.update(packet.getDocumentSignature().getBytes());
        boolean verify = signature.verify(decodeSignature);
        return verify;
    }
}


