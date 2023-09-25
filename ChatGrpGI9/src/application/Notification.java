package application;

import java.io.Serializable;

public class Notification implements Serializable{
    private String sender;
    private String receiver;
    private Message message;
    private int type;// 0 => invitation
                     // 1 => request
                     // 2 => normal message
    public Notification(String sender, String receiver, Message message, int type){
        this.sender=sender;
        this.receiver=receiver;
        this.message=message;
        this.type=type;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if(type==0){
            return "You have a new group invitation : "+this.getSender();
        }else if(type==1){
            return "You have new group joining request for \""+this.getReceiver()+"\" group from "+this.getSender();
        }
        if(message!=null && message.getType()==1)
            return sender+" sent you a file.";
        return "New message from "+sender+" : "+message.getMessage();
    }

//    @Override
//    public boolean equals(Object obj) {//to get message count in main controller
//        if(obj==null)
//            return false;
//        Notification n=(Notification) obj;
//        if(this.sender!=null && this.sender.equals(n.getSender()))
//            return true;
//        else
//            return false;
//    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getType(){
        return type;
    }
}
