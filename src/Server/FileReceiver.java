package Server;

import application.Packet;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class FileReceiver {
    public static String receiveFile(Packet packet) throws IOException {
        String ext=packet.string3;//extension of the file
        String filename;
        filename="src/resources/server_resources/ChatGiGrp9_"+getRandomString()+"."+ext;
        OutputStream out = new FileOutputStream(filename);
        out.write(packet.buff);
        out.close();
        return filename;
    }
    private static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}
