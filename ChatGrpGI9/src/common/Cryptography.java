package common;

import application.Packet;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Cryptography {
    static CryptoUtil cryptoUtil = new CryptoUtil();
    static String publicKey;

    static {
        try {
            publicKey = cryptoUtil.publicKeyFromCertificate("certification.cert");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Cryptography() throws Exception {
    }

    public static SealedObject encrypt(Packet packet){
        Cipher cipher= null;
        SealedObject sealedObject=null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec sks = new SecretKeySpec(publicKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            String signature=cryptoUtil.hmacSign(packet.getDocumentSignature().getBytes(),publicKey);
            packet.setSignature(signature);
            sealedObject=new SealedObject(packet,cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | IOException |
                 InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sealedObject;
    }
    public static Packet decrypt(SealedObject sealedObject) throws Exception {
        Cipher cipher= null;
        Packet packet=null;
        boolean isVerified=cryptoUtil.hmacVerify(sealedObject,publicKey);
        System.out.println(isVerified ?"Signature OK":"Signature Not OK");
        if(!isVerified){
            throw new Exception("Signature is not verified");
        }
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec sks = new SecretKeySpec(publicKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            packet=(Packet) sealedObject.getObject(cipher);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return packet;
    }
    public static Packet decryptSignature(SealedObject sealedObject) throws Exception {
        Cipher cipher= null;
        Packet packet=null;

        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec sks = new SecretKeySpec(publicKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, sks);
            packet=(Packet) sealedObject.getObject(cipher);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return packet;
    }
}