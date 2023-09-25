package Server;

import application.Main;
import application.Notification;
import application.Packet;
import application.People;
import common.Cryptography;
import common.FileSharing;

import javax.crypto.SealedObject;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ServerReceivingThread implements Runnable {
    private ObjectInputStream ois;
    private InputStream is;
    private ObjectOutputStream oos;
    private OutputStream os;
    private Socket clientSocket;
    private PreparedDatabaseProxy db;
    public ServerReceivingThread(Socket clientSocket) throws IOException {
        db=new PreparedDatabaseProxy();
        this.clientSocket=clientSocket;
        os=clientSocket.getOutputStream();
        is=clientSocket.getInputStream();
        ois=new ObjectInputStream(is);
        oos=new ObjectOutputStream(os);
    }

    @Override
    public void run() {
        while(true) {
            try {

//                FileReceiver.sendFile(os);

//                Packet receivedPacket = (Packet) ois.readObject();
                Packet receivedPacket= Cryptography.decrypt((SealedObject)ois.readObject());
                System.out.println("Packet received by server operation: "+receivedPacket.operation);
                boolean isLogout=handlePacket(receivedPacket);
                if(isLogout)
                    break;
//                oos.flush();
//                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public boolean handlePacket(Packet p) throws SQLException, IOException, ClassNotFoundException, MessagingException {
        Packet reply;
        if(p.operation.equalsIgnoreCase("signUp")) {
            String filename=null;
            if(p.string3!=null){
                filename=FileReceiver.receiveFile(p);
            }
            db.signup(filename,p);
            //sending information of new signup to all currently online users
            reply=new Packet("newUser");
            People newUser=new People(p.string1,1,0,p.string4,p.string5,db.getPhoto(p.string1));
            reply.peopleList.add(newUser);
            sendToEverybody(reply);
        }else if(p.operation.equalsIgnoreCase("update")){
            String username = p.matricule;
            System.out.println(username);
            String filename=null;
            if(p.string3!=null){
                filename=FileReceiver.receiveFile(p);
            }

            db.update(filename,p);
            //sending information of new signup to all currently online users

        }else if(p.operation.equalsIgnoreCase("login")){
            reply=new Packet("loginResponse");
            reply.string1=p.string1;
            reply.string2=p.string2;
            System.out.println("username : "+p.string1);
            if(db.login(reply)){//set everything in reply packet
                //login successful
                reply.response=true;
            }else{
                reply.response=false;
            }
            oos.flush();
            System.out.println("Writing object:"+reply.operation);
            oos.writeObject(Cryptography.encrypt(reply));
            if(reply.response){
                ServerMain.addMappingOos(p.string1,oos);
                ServerMain.addMappingOs(p.string1,os);
            }
        }else if(p.operation.equalsIgnoreCase("peopleList")){
            reply=new Packet("peopleList");
          //  String username = Main.getUser().getUserName();
            System.out.println(p.string1);
            db.setPeopleList(reply.peopleList,p.string1);
            for(People pp : reply.peopleList){
                if(ServerMain.getMappingOos(pp.getUserName())!=null)
                    pp.setOnlineStatus(1);
            }
            oos.flush();
            oos.writeObject(Cryptography.encrypt(reply));
        }else if (p.operation.equalsIgnoreCase("notificationsList")){
            reply=new Packet("notificationsList");
            db.setNotificationsList(reply.notificationsList, p.string1,null,0);
            oos.flush();
            oos.writeObject(Cryptography.encrypt(reply));
        }else if (p.operation.equalsIgnoreCase("messagesList")){
            reply=new Packet("messagesList");
            db.setMessagesList(reply.messagesList,p.string1,p.string2);
            oos.flush();
            oos.writeObject(Cryptography.encrypt(reply));
        }else if (p.operation.equalsIgnoreCase("newMessage")){
            reply=new Packet("notificationsList");
            int mType=p.messagesList.get(0).getType();
            if(mType==1){//file message
                System.out.println("Receiving file");
                p.messagesList.get(0).setMessage(FileReceiver.receiveFile(p));
                System.out.println("here");
            }
            System.out.println("Database entry url: "+p.messagesList.get(0).getMessage());
            db.updateMessages(p.messagesList.get(0));//message contain the complete url
            String fromUser=p.messagesList.get(0).getSender();
            if(db.isGroup(fromUser)){
                //send to all online users
                List<String> participants=new ArrayList<>();
                db.getParticipants(fromUser,participants);
                for(String pp : participants) {
                    System.out.println("Participant: "+pp);
                    if(pp.equals(p.messagesList.get(0).getReceiver()))
                        continue;
                    ObjectOutputStream toos = ServerMain.getMappingOos(pp);
                    if (toos == null) {
                        System.out.println(p.messagesList.get(0).getReceiver() + " is not online");
                        continue;
                    }
                    reply.notificationsList.clear();//found a bug here wooff
                    db.setNotificationsList(reply.notificationsList, pp, fromUser, 1);
                    toos.flush();
                    for (Notification nn : reply.notificationsList) {
                        System.out.println("Writing to toos:" + nn);
                    }
                    toos.writeObject(Cryptography.encrypt(reply));
                }
                return false;
            }
            //get the socket of receiver
            ObjectOutputStream toos=ServerMain.getMappingOos(p.messagesList.get(0).getReceiver());
            if(toos==null){
                System.out.println(p.messagesList.get(0).getReceiver()+" is not online");
                return false;
            }
            db.setNotificationsList(reply.notificationsList, p.messagesList.get(0).getReceiver(),p.messagesList.get(0).getSender(),1);
            toos.flush();
            System.out.println("Writing to toos:"+reply);
            toos.writeObject(Cryptography.encrypt(reply));
        }else if(p.operation.equalsIgnoreCase("getFile")){
            reply=new Packet("fileData");
            reply.string1=p.string1;//filename with path
            db.setMessageStatus(p.messagesList.get(0));
            FileSender.sendFile(reply);
            oos.writeObject(Cryptography.encrypt(reply));
//            oos.flush();
//            FileSharing.sendFile(oos,reply);
        }else if(p.operation.equalsIgnoreCase("fileData")){
            FileSharing.receiveFile(ois,p);
        }else if (p.operation.equalsIgnoreCase("createGroup")){
            String filename=null;
            if(p.string3!=null){
                filename=FileReceiver.receiveFile(p);
            }
            db.createGroup(filename,p);

            //sending information of new group to all currently online users

            reply=new Packet("newUser");
            People  newGroup=new People(p.string1,0,1,null,null,db.getPhoto(p.string1));
            List<String> list=new ArrayList<>();
            db.getParticipants(p.string1,list);
            newGroup.setParticipants(list);
            newGroup.setAdmin(db.getAdmin(p.string1));

            //reply.peopleList.add(newGroup);
            //sendToEverybody(reply);

            //sending invitation to all currently online users
            for(People pp : p.peopleList){
                ObjectOutputStream toos=null;
                if((toos=ServerMain.getMappingOos(pp.getUserName()))!=null){//user is online
                    reply=new Packet("notificationsList");
                    db.setNotificationsList(reply.notificationsList, pp.getUserName(),null,0);
                    toos.writeObject(Cryptography.encrypt(reply));
                }
            }

        }else if (p.operation.equalsIgnoreCase("checkAvailability")){
            reply=new Packet("checkAvailability");
            reply.type=p.type;
            reply.response=db.isAvailable(p.string1);
            oos.writeObject(Cryptography.encrypt(reply));
        }else if (p.operation.equalsIgnoreCase("checkAvailability1")){
            reply=new Packet("checkAvailability1");
            reply.type=p.type;
            reply.response=db.isAvailable(p.string1);
            oos.writeObject(Cryptography.encrypt(reply));
        }else if(p.operation.equalsIgnoreCase("acceptInvitation")){
            db.acceptInvitation(p.string1,p.string2);
            reply=new Packet("peopleList");
            db.setPeopleList(reply.peopleList,p.string1);
            //tell everybody that an invitation has been accepted
            //in place of sending complete list, only the info of changing group should be sent
            reply=new Packet("groupUpdate");
            reply.string1=p.string1;//group name
            reply.string2=p.string2;//new username
            sendToEverybody(reply);

        }else if(p.operation.equalsIgnoreCase("rejectInvitation")){
            db.rejectInvitation(p.string1,p.string2);
            reply=new Packet("peopleList");
            db.setPeopleList(reply.peopleList,p.string1);
        }else if(p.operation.equalsIgnoreCase("deleteGrp")){
            db.deleteGrp(p.string1);
        }else if(p.operation.equalsIgnoreCase("groupRequest")){
            db.groupRequest(p.string1,p.string2);

            //for online updating
            String admin=db.getAdmin(p.string1);
            ObjectOutputStream toos=null;
            if((toos=ServerMain.getMappingOos(admin))!=null){//i.e. admin is online
                reply=new Packet("notificationsList");
                db.setNotificationsList(reply.notificationsList, admin,null,0);
                toos.writeObject(Cryptography.encrypt(reply));
            }

        }else if(p.operation.equalsIgnoreCase("acceptRequest")){
            db.acceptRequest(p.string1,p.string2);

            //for online updating
            //tell everybody that an invitation has been accepted
            //in place of sending complete list, only the info of changing group should be sent
            reply=new Packet("groupUpdate");
            reply.string1=p.string2;//group name
            reply.string2=p.string1 ;//new username
            sendToEverybody(reply);
            //if(reply.response){
              //  ServerMain.addMappingOos(p.string1,oos);
                //ServerMain.addMappingOs(p.string1,os);
           // }

        }else if(p.operation.equalsIgnoreCase("rejectRequest")){
            db.rejectRequest(p.string1,p.string2);
        }else if(p.operation.equalsIgnoreCase("logout")){
            //remove mapping
            String username=p.string1;
            if(ServerMain.getMappingOos(username)==null){
                reply=new Packet("logout");
                oos.writeObject(Cryptography.encrypt(reply));
                clientSocket.close();
                return true;
            }
            ServerMain.removeMapping(username);
            //send information to everybody
            reply=new Packet("offlineUsers");
            reply.peopleList.add(new People(username,1,0,db.getEmail(username),db.getContact(username),db.getPhoto(username)));
            sendToEverybody(reply);
            //send to closing user
            reply=new Packet("logout");
            oos.writeObject(Cryptography.encrypt(reply));
            //close the socket
            clientSocket.close();
            return true;//to break the infinite loop
        }else if(p.operation.equalsIgnoreCase("deleteuser")){
            String username = p.string1;
            System.out.println(username);
            db.deleteuser(username);
            System.out.println(username);
        }
        return false;
    }
    public void sendToEverybody(Packet p) throws IOException {
        Map<String,ObjectOutputStream> cmap=ServerMain.getConnectionMap();
        for (Map.Entry<String,ObjectOutputStream> entry : cmap.entrySet()){
            entry.getValue().writeObject(Cryptography.encrypt(p));
        }
    }
}
