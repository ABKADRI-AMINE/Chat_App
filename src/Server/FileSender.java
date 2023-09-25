package Server;


import application.Packet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSender {
    public static void sendFile(Packet packet) throws IOException {
        InputStream in = new FileInputStream(packet.string1);//string1 contains the path
        byte[] buf = new byte[104857600];// 100 MB
        int len = in.read(buf);
        packet.buff=buf;
        return;
    }
}
