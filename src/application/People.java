package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class People implements Serializable{
    private String userName;
    private String email;
    private String contact;
    private byte[] buff;
    private int isOnline;
    private int type;
    private int unreadMessageCount;//I think this is not a good design practice
    private List<String> participants;//used in case of group
    private String admin;//used in case of group
    public People(String userName,int isOnline,int type, String email, String contact, byte[] buff){
        this.userName=userName;
        this.isOnline=isOnline;
        this.type=type;
        this.email=email;
        this.contact=contact;
        this.buff=buff;
        participants=new ArrayList<>();
    }
    public boolean isParticipant(String username){
        if(participants.contains(username))
            return true;
        else
            return false;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }
    public void addParticipant(String username){
        participants.add(username);
    }
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getAdmin() {
        return admin;
    }

    public void setUserName(String un){
        userName=un;
    }
    public String getUserName() {
        return userName;
    }
    public int getOnlineStatus() {
        return isOnline;
    }
    public String getEmail() {
        return email;
    }
    public String getContact() {
        return contact;
    }
    public void setOnlineStatus(int status){
        isOnline=status;
    }
    public int getType() {
        return type;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj==null)
            return true;
        People people=(People)obj;
        return people.getUserName().equals(userName);
    }
    @Override
    public String toString() {
        return userName;
    }
    public byte[] getBuff() {
        return buff;
    }
}
