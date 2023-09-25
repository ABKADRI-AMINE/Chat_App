package common;

import application.Packet;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CryptographyRSA {


    public CryptographyRSA() throws Exception {
    }


    public static List<SealedObject> encryptRSA(Packet packet) throws Exception {
        CryptoUtil cryptoUtil = new CryptoUtil();
        PublicKey publicKey = cryptoUtil.publicKeyFromCertificateRSA("Certification.cert");
        List<SealedObject> encryptedBlocks = new ArrayList<>();

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] data = packet.getDocumentSignature().getBytes(); // Convert the data to bytes


        // Define the block size (smaller than the RSA key size)
        int blockSize = (cipher.getBlockSize() / 8) - 11;
        int numBlocks = (data.length + blockSize - 1) / blockSize; // Calculate the number of blocks

        // Split the data into blocks and encrypt each block
        for (int i = 0; i < numBlocks; i++) {
            int startIndex = i * blockSize;
            int endIndex = Math.min(startIndex + blockSize, data.length);
            byte[] block = Arrays.copyOfRange(data, startIndex, endIndex);
            SealedObject sealedObject = new SealedObject(block, cipher);
            encryptedBlocks.add(sealedObject);
        }

        return encryptedBlocks;
    }
    public static Packet decryptRSA(List<SealedObject> sealedObjects, PrivateKey privateKey) throws Exception {
        CryptoUtil cryptoUtil = new CryptoUtil();

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Decrypt each sealed object and write the decrypted data to the output stream
        for (SealedObject sealedObject : sealedObjects) {
            byte[] decryptedBlock = (byte[]) sealedObject.getObject(cipher);
            outputStream.write(decryptedBlock);
        }

        byte[] decryptedData = outputStream.toByteArray();

        // Convert the decrypted data to a Packet object
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decryptedData);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Packet packet = (Packet) objectInputStream.readObject();

        objectInputStream.close();
        inputStream.close();
        outputStream.close();

        return packet;
    }
}