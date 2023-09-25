package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Packet implements Serializable{
    public String operation;
    public String string1;//username
    public String string2;//password
    public String string3;//file extension
    public String string4;
    public String string5;
    public String matricule;//usename used in update
    public int type;
    public boolean response;//response of some checking
    public List<People> peopleList;//contains all the users
    public List<Notification> notificationsList;
    public List<Message> messagesList;
    public byte[] buff;
    public Packet(String operation){
        this.operation=operation;
        peopleList= new ArrayList<>();
        notificationsList=new ArrayList<>();
        messagesList=new ArrayList<>();
        buff=new byte[81920];
    }

    @Override
    public String toString() {
        return operation;
    }
}
