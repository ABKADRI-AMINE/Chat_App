package application;

import common.Cryptography;
import common.FileSharing;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ClientReceivingThread implements Runnable{
    private Socket socket;
    private ObjectInputStream ois;
    private Packet receivedPacket;
    private InputStream is;
    private MainController mainController;
    public ClientReceivingThread(Socket socket) throws IOException {
        this.socket=socket;
        is=socket.getInputStream();
        ois=new ObjectInputStream(is);
    }

    @Override
    public void run() {
        while (true) {
            try {
                receivedPacket=Cryptography.decrypt((SealedObject) ois.readObject());
                System.out.println("Packet received by client operation: "+receivedPacket.operation);
                if(receivedPacket.operation.equalsIgnoreCase("logout")){
                    socket.close();
                    break;
                }
                handlePacket(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void handlePacket(Packet p) throws IOException, ClassNotFoundException {
        if(p.operation.equalsIgnoreCase("loginResponse")){
            if(p.response) {
                System.out.println("Login successful");
                Main.setUser(new People(p.string1,1,0,p.string3,p.string4,p.buff));
                ClientSender.setUser(new People(p.string1,1,0,p.string3,p.string4,p.buff));
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/main.fxml"));
                Parent root=fxmlLoader.load();
                mainController=fxmlLoader.<MainController>getController();
                mainController.myInitializations();
                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    stage.setTitle("Let's chat " + Main.getUser().getUserName());
                    stage.setScene(new Scene(root, 1000, 550));
                    stage.setOnCloseRequest(event -> {

                        System.out.println("Stage closed");
                        //send the logout information to server
                        try {
                            ClientSender.logout();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //close the appropriate sockets

                    });
                    stage.show();
                });
            }else {
                System.out.println("Login unsuccessful");
            }
            Platform.runLater(() -> {
                LoginController.loginResponse(p.response);
            });
        }else if(p.operation.equalsIgnoreCase("checkAvailability")) {
            System.out.println("Type in the packet : "+p.type);
            Platform.runLater(() -> {
                if(p.type==0)
                    SignUpController.setAvailability(p.response);
                else
                    GroupController.setAvailability(p.response);
            });
        }else if(p.operation.equalsIgnoreCase("checkAvailability1")) {
            System.out.println("Type in the packet : "+p.type);
            Platform.runLater(() -> {
                if(p.type==0)
                    UpdateController.setAvailability1(p.response);
                else
                    GroupController.setAvailability1(p.response);
            });
        }else if (p.operation.equalsIgnoreCase("peopleList")){
            mainController.setPeopleList(p.peopleList);
        }else if (p.operation.equalsIgnoreCase("notificationsList")){
            mainController.setNotificationList(p.notificationsList);
        }else if (p.operation.equalsIgnoreCase("messagesList")){
            mainController.setMessageList(p.messagesList);
        }else if(p.operation.equalsIgnoreCase("onlineUsers")){
            mainController.setPeopleList(p.peopleList);
        }else if(p.operation.equalsIgnoreCase("fileData")){
            String newPath="/src/resources/client_resources/"+p.string1.substring(p.string1.lastIndexOf('/')+1);
            FileReceiver.receiveFile(p);
        }else if(p.operation.equalsIgnoreCase("groupUpdate")){
            String groupName=p.string1;
            String newUser=p.string2;
            mainController.updatePeopleList(groupName,newUser);
        }else if(p.operation.equalsIgnoreCase("newUser")){
            mainController.addPeople(p.peopleList.get(0));
        }else if(p.operation.equalsIgnoreCase("offlineUsers")){
            mainController.changeOnlineStatus(p.peopleList.get(0));
        }
    }
}
