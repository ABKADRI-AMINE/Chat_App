package Server;

import application.Packet;
import application.People;
import common.Cryptography;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerMain {
    private static ServerSocket serverSocket;
    private static Map<String,ObjectOutputStream> connectionMap;
    private static Map<String,OutputStream> connectionMap2;
    private static PreparedDatabaseProxy db;
    public static synchronized void addMappingOos(String userName, ObjectOutputStream os) throws IOException, SQLException {
        connectionMap.put(userName, os);
        //whenever add mapping is called
        //broadcast to all users that a new user is online
        Packet sendingPacket=new Packet("onlineUsers");
        int userType=0;
        if(db.isGroup(userName))
            userType=1;
        sendingPacket.peopleList.add(new People(userName,1,userType,db.getEmail(userName),db.getContact(userName),db.getPhoto(userName)));
        List<People> temp=new ArrayList<>();
        for (Map.Entry<String,ObjectOutputStream> entry : connectionMap.entrySet()){
            if(!entry.getKey().equals(userName)) {
                entry.getValue().writeObject(Cryptography.encrypt(sendingPacket));
                if(db.isGroup(entry.getKey()))
                    userType=1;
                else
                    userType=0;
                temp.add(new People(entry.getKey(), 1,userType,db.getEmail(entry.getKey()),db.getContact(entry.getKey()),db.getPhoto(entry.getKey())));
            }
        }
        sendingPacket.peopleList=temp;
        connectionMap.get(userName).writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static synchronized void addMappingOs(String userName, OutputStream os){
        connectionMap2.put(userName,os);
    }
    public static synchronized ObjectOutputStream getMappingOos(String userName){
        return connectionMap.get(userName);
    }
    public static synchronized OutputStream getMappingOs(String userName) {
        return connectionMap2.get(userName);
    }

    public static synchronized void removeMapping(String userName){
        connectionMap.remove(userName);
    }

    public static synchronized Map<String, ObjectOutputStream> getConnectionMap() {
        return connectionMap;
    }

    public static void main(String args[]) throws Exception{
//        System.out.println(System.getProperty("user.dir"));
//        System.out.println(Hash.toSHA1("garvit"));
        db=new PreparedDatabaseProxy();
        serverSocket=new ServerSocket(8888);
        connectionMap=new HashMap<>();
        connectionMap2=new HashMap<>();
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("client connected");
            ServerReceivingThread serverReceivingThread=new ServerReceivingThread(clientSocket);
            Thread t=new Thread(serverReceivingThread);
            t.start();
        }
    }
}
