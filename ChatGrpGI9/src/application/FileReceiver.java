package application;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileReceiver {
    public static void receiveFile(Packet packet) throws IOException {
        String ext=packet.string3;
        String filename;
        filename="src/resources/client_resources/"+packet.string1.substring(packet.string1.lastIndexOf('/')+1);
        OutputStream out = new FileOutputStream(filename);
        out.write(packet.buff);
        out.close();
    }
}
