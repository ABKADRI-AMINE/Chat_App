package application;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable{
    private String message;
    private Timestamp timestamp;
    private String sender;
    private String receiver;
    private String actualSender;
    private int read;
    private int type;
    public Message(String message, Timestamp timestamp, String sender,String receiver,int read,int type,String actualSender){
        this.message=message;
        this.timestamp=timestamp;
        this.sender=sender;
        this.read=read;
        this.receiver=receiver;
        this.type=type;
        this.actualSender=actualSender;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getRead() { return read; }

    public int getType(){
        return type;
    }

    public String getActualSender() {
        return actualSender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRead(int read) {
        this.read = read;
    }
}
