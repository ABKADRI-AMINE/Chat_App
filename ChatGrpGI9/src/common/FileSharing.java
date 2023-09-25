package common;

import application.Packet;

import java.io.*;
import java.util.Random;

public class FileSharing {
    public static void sendFile(ObjectOutputStream oos,Packet packet) throws IOException {
        InputStream in = new FileInputStream(packet.string1);//string1 contains the path
        byte[] buf = new byte[81920];// 80 KB
        int len = in.read(buf);
        packet.buff=buf;
//        copy(in, out);
//        out.write(-1);
//        out.flush();
        oos.writeObject(packet);
        in.close();
    }
    public static String receiveFile(ObjectInputStream ois,Packet packet) throws IOException, ClassNotFoundException {
        String ext=packet.string1.substring(packet.string1.lastIndexOf('.')+1);
        String name=packet.string3;
        String filename;
        if(name==null)//generate random filename when file is coming to server
            filename="src/resources/server_resources/ChatGiGrp9_"+getRandomString()+"."+ext;
        else//when client receives the file
            filename=name;
        OutputStream out = new FileOutputStream(filename);
//        copy(in, out);
        out.write(packet.buff);
        out.close();
        return filename;
    }
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[81920];// 80 KB
        int len = 0;
//        while ((len = in.read(buf)) !=-1) {
//            if(buf[len-1]==-1)//a hack devised by me to indicate end of stream
//                break;
//            out.write(buf, 0, len);
//        }
        len = in.read(buf);
        out.write(buf, 0, len);
        System.out.println("infinite loop");
    }
    public static String getRandomString() {
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
