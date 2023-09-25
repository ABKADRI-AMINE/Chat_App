package application;


import common.Cryptography;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ClientSender {
    private static Socket socket;
    private static ObjectOutputStream oos;
    private static OutputStream os;
    private static Packet sendingPacket;
    private static People user;
    public ClientSender(People user) throws IOException {
//        os=socket.getOutputStream();
//        oos=new ObjectOutputStream(os);
//        this.user=user;
    }

    public static void setSocket(Socket soc) throws IOException{
        socket=soc;
        os=socket.getOutputStream();
        oos=new ObjectOutputStream(os);
    }
    public static void setUser(People p){
        user=p;
    }
    public static void createGroup(String name, List<People> participants,File file) throws IOException {
        sendingPacket=new Packet("createGroup");
        sendingPacket.string1=name;//name of group
        sendingPacket.string2=Main.getUser().getUserName();//name of the admin
        sendingPacket.peopleList.addAll(participants);
        if(file!=null) {
            String path=file.getAbsolutePath();
            sendingPacket.string3=path.substring(path.lastIndexOf('.')+1);//extension of file in string 3
            sendingPacket.buff = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        }
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void signup(File file, String username, String password, String email, String contact) throws IOException{
        sendingPacket=new Packet("signUp");
        sendingPacket.string1=username;
        sendingPacket.string2=password;
        sendingPacket.string4=email;
        sendingPacket.string5=contact;
        if(file!=null) {
            String path=file.getAbsolutePath();
            sendingPacket.string3=path.substring(path.lastIndexOf('.')+1);//extension of file in string 3
            sendingPacket.buff = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        }
        oos.writeObject(Cryptography.encrypt(sendingPacket));

    }

    public static void update(File file, String username, String password, String email, String contact, String username1) throws IOException{
        sendingPacket=new Packet("update");
        sendingPacket.string1=username;
        sendingPacket.string2=password;
        sendingPacket.string4=email;
        sendingPacket.string5=contact;
        sendingPacket.matricule = username1;
        if(file!=null) {
            String path=file.getAbsolutePath();
            sendingPacket.string3=path.substring(path.lastIndexOf('.')+1);//extension of file in string 3
            sendingPacket.buff = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        }
        oos.writeObject(Cryptography.encrypt(sendingPacket));

    }

    public static void checkAvailability(String username,int type) throws IOException{
        sendingPacket=new Packet("checkAvailability");
        sendingPacket.string1=username;
        sendingPacket.type=type;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void login(String username, String password) throws IOException{
        sendingPacket=new Packet("login");
        sendingPacket.string1=username;
        sendingPacket.string2=password;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
//        oos.flush();
    }
    public static void peopleList() throws IOException{
        sendingPacket=new Packet("peopleList");
        sendingPacket.string1 = Main.getUser().getUserName();
        oos.writeObject(Cryptography.encrypt(sendingPacket));
//        oos.flush();
        if(Main.isConnected==1){

        }else{
            //fetch from local files
        }
    }
    public static void notificationsList() throws IOException{
        sendingPacket=new Packet("notificationsList");
        sendingPacket.string1=user.getUserName();
        oos.writeObject(Cryptography.encrypt(sendingPacket));
//        oos.flush();
    }
    public static void fetchMessages(String fromUser) throws IOException{
        sendingPacket=new Packet("messagesList");
        sendingPacket.string1=user.getUserName();
        sendingPacket.string2=fromUser;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
//        oos.flush();
    }
    public static void sendMessage(Message message) throws IOException {
        sendingPacket=new Packet("newMessage");
        sendingPacket.messagesList.add(message);
        oos.writeObject(Cryptography.encrypt(sendingPacket));
//        oos.flush();
    }
    public static void sendFile(String path, Message m) throws IOException{
        sendingPacket=new Packet("newMessage");
        FileSender.sendFile(path,sendingPacket);
        sendingPacket.messagesList.add(m);
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void getFile(Message message) throws IOException{
        String fileName=message.getMessage();
        sendingPacket=new Packet("getFile");
        sendingPacket.string1=fileName;
        sendingPacket.string2=fileName.substring(fileName.lastIndexOf(".")+1);
        sendingPacket.messagesList.add(message);
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void acceptInvitation(String groupName) throws IOException{
        sendingPacket=new Packet("acceptInvitation");
        sendingPacket.string1=groupName;
        sendingPacket.string2=Main.getUser().getUserName();
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void rejectInvitation(String groupName) throws IOException{
        sendingPacket=new Packet("rejectInvitation");
        sendingPacket.string1=groupName;
        sendingPacket.string2=Main.getUser().getUserName();
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void sendGroupRequest(String groupName) throws IOException {
        sendingPacket=new Packet("groupRequest");
        sendingPacket.string1=groupName;
        sendingPacket.string2=Main.getUser().getUserName();
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void acceptRequest(String fromUser, String groupName) throws IOException{
        sendingPacket=new Packet("acceptRequest");
        sendingPacket.string1=fromUser;
        sendingPacket.string2=groupName;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void rejectRequest(String fromUser, String groupName) throws IOException{
        sendingPacket=new Packet("rejectRequest");
        sendingPacket.string1=fromUser;
        sendingPacket.string2=groupName;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void logout() throws IOException{
        sendingPacket=new Packet("logout");
        if(Main.getUser()!=null)
            sendingPacket.string1=Main.getUser().getUserName();
        else
            sendingPacket.string1=null;//closed during login
        System.out.println("sending packet: "+sendingPacket.operation);
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }

    public static void checkAvailability1(String username,int type) throws IOException{
        sendingPacket=new Packet("checkAvailability1");
        sendingPacket.string1=username;
        sendingPacket.type=type;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void delete(String groupeName) throws IOException{
        sendingPacket=new Packet("deleteGrp");
        sendingPacket.string1=groupeName;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }
    public static void deleteuser(String username) throws IOException{
        sendingPacket=new Packet("deleteuser");
        sendingPacket.string1=username;
        oos.writeObject(Cryptography.encrypt(sendingPacket));
    }

}
