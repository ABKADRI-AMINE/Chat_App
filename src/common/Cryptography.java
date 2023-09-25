package common;

import application.Packet;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Cryptography {
    public static SealedObject encrypt(Packet packet){
        Cipher cipher= null;
        SealedObject sealedObject=null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            sealedObject=new SealedObject(packet,cipher);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return sealedObject;
    }
    public static Packet decrypt(SealedObject sealedObject){
        Cipher cipher= null;
        Packet packet=null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
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
