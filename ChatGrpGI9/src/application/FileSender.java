package application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;

public class FileSender {
    public static void sendFile(String path, Packet packet) throws IOException {
        InputStream in = new FileInputStream(path);
        byte[] buf = new byte[8388608];// 8 MB
        int len = in.read(buf);
        packet.buff=buf;
        packet.string3=path.substring(path.lastIndexOf('.')+1);
        return;
    }
}
