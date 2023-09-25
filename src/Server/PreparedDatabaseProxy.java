package Server;

//Exactly same as DatabaseProxy Class but prepared statements are used.

import application.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PreparedDatabaseProxy {
    private Connection conn;
    public PreparedDatabaseProxy(){
        try {
            conn= DriverManager.getConnection("jdbc:mysql://localhost:3306/chatgi","root","toor");
            System.out.println("Connection to the database has been established!!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("problem in connection with the database");
        }
    }
    public synchronized String getEmail(String username) throws SQLException {
        String query="SELECT * FROM users WHERE username=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,username);
        ResultSet rs=stmt.executeQuery();
        while(rs.next())
            return rs.getString("email");
        return null;
    }
    public synchronized String getContact(String username) throws SQLException {
        String query="SELECT * FROM users WHERE username=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,username);
        ResultSet rs=stmt.executeQuery();
        while(rs.next())
            return rs.getString("contact");
        return null;
    }
    public synchronized byte[] getPhoto(String username) throws SQLException, IOException {
        String path=null;
        String query="SELECT * FROM users WHERE username=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,username);
        ResultSet rs=stmt.executeQuery();
        while(rs.next())
            path=rs.getString("photo");
        if(path!=null){
            Packet temp=new Packet("tempPacket");
            temp.string1=path;
            FileSender.sendFile(temp);
            return temp.buff;
        }
        return null;
    }
    public synchronized void signup(String filename,Packet p) throws SQLException {
        String query;
//        if(filename!=null)
//            query="INSERT INTO users values(null,'"+p.string1+"','"+p.string2+"',0,'"+p.string4+"','"+p.string5+"','"+filename+"')";
//        else
//            query="INSERT INTO users values(null,'"+p.string1+"','"+p.string2+"',0,'"+p.string4+"','"+p.string5+"',null)";
        query="INSERT INTO users values(null,?,?,0,?,?,?)";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,p.string1);
        stmt.setString(2,p.string2);
        stmt.setString(3,p.string4);
        stmt.setString(4,p.string5);
        stmt.setString(5,filename);
        stmt.executeUpdate();
    }
    public synchronized void update(String filename,Packet p) throws SQLException {
        String query;

        query = "UPDATE users SET username=? ,password = ? , isGroup=0,email=?,contact=?,photo=? WHERE username= ?";

        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,p.string1);
        stmt.setString(2,p.string2);
        stmt.setString(3,p.string4);
        stmt.setString(4,p.string5);
        stmt.setString(5,filename);
        stmt.setString(6,p.matricule);

        stmt.executeUpdate();
    }
    public synchronized boolean login(Packet packet) throws SQLException, IOException {
        String username=packet.string1;
        String password=packet.string2;
        String query="SELECT * FROM users WHERE username=? AND password=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,username);
        stmt.setString(2,password);
        ResultSet rs=stmt.executeQuery();
        if(rs.next()){
            packet.string3=rs.getString("email");
            packet.string4=rs.getString("contact");
            packet.buff=getPhoto(packet.string1);
            return true;
        }else{
            return false;
        }
    }
    public synchronized void setPeopleList(List<People> peopleList, String username1) throws SQLException, IOException {
        String query="SELECT * FROM users;";
        PreparedStatement stmt=conn.prepareStatement(query);
        ResultSet rs=stmt.executeQuery();

        while (rs.next()){
            String username=rs.getString("username");
            People pp=new People(username,0,rs.getInt("isGroup"),rs.getString("email"),rs.getString("contact"),getPhoto(username));

            if(rs.getInt("isGroup")==1){//it is a group
                String query1 = "SELECT * FROM " + username + " WHERE participants = ?";
                PreparedStatement stmt1=conn.prepareStatement(query1);
                stmt1.setString(1, username1);
                ResultSet rs1=stmt1.executeQuery();
                if (!rs1.next()) {
                    System.out.println("is empty");
                } else {
                    // result set is not empty
                    // handle the case when there is at least one row returned
                    //add participants
                    List<String> list=new ArrayList<>();
                    getParticipants(username,list);
                    pp.setParticipants(list);
                    pp.setAdmin(getAdmin(username));
                    peopleList.add(pp);

                }
                rs1.close();
                stmt1.close();
            }else{
                peopleList.add(pp);
            }
        }
        rs.close();
        stmt.close();
    }

    public synchronized void setNotificationsList(List<Notification> notificationsList, String userName, String senderName, int online) throws SQLException {
        String query="SELECT * FROM messages WHERE messageto=? AND sent=0 order by date";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,userName);
        ResultSet rs=stmt.executeQuery();
        while(rs.next()){
            String sender=rs.getString("messagefrom");
            String receiver=userName;
            Message message=new Message(rs.getString("message"),rs.getTimestamp("date"),sender,receiver,rs.getInt("sent"),rs.getInt("type"),rs.getString("actualsender"));
            notificationsList.add(new Notification(sender,receiver,message,2));
        }
        //notifications for group invitations
        query="SELECT * FROM invitations WHERE touser=? AND type=0";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,userName);
        rs=stmt.executeQuery();
        while(rs.next()){
            String messagefrom=rs.getString("fromuser");
            notificationsList.add(new Notification(messagefrom,userName,null,0));
        }
        //notifications for group requests
        query="SELECT * FROM invitations WHERE type=1";
        stmt=conn.prepareStatement(query);
        rs=stmt.executeQuery();
        while(rs.next()){
            String groupName=rs.getString("touser");
            if(isAdmin(groupName,userName)){
                notificationsList.add(new Notification(rs.getString("fromuser"),groupName,null,1));
            }
        }

        if(online==1) {
            query = "UPDATE messages SET sent=1 WHERE messageto=? AND messagefrom=? AND type=0";
            stmt=conn.prepareStatement(query);
            stmt.setString(1,userName);
            stmt.setString(2,senderName);
            stmt.executeUpdate();
        }
        return;
    }
    public synchronized boolean isAdmin(String groupName, String userName) throws SQLException {
        String query="SELECT * from "+groupName+" WHERE participants=? AND isAdmin=1";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,userName);
        ResultSet rs=stmt.executeQuery();
        return rs.next();
    }
    public synchronized void setMessagesList(List<Message> messageList,String toUser,String fromUser) throws SQLException {
        String query="SELECT * FROM messages WHERE (messageto=? AND messagefrom=?) OR (messagefrom=? AND messageto=?) order by date";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,toUser);
        stmt.setString(2,fromUser);
        stmt.setString(3,toUser);
        stmt.setString(4,fromUser);
        ResultSet rs=stmt.executeQuery();
        while(rs.next()){
            messageList.add(new Message(rs.getString("message"),rs.getTimestamp("date"),rs.getString("messagefrom"),rs.getString("messageto"),rs.getInt("sent"),rs.getInt("type"),rs.getString("actualsender")));
        }
        query="UPDATE messages SET sent=1 WHERE messageto=? AND messagefrom=? AND type=0";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,toUser);
        stmt.setString(2,fromUser);
        stmt.executeUpdate();
        return;
    }
    public synchronized void updateMessages(Message message) throws SQLException {
        PreparedStatement stmt=null;
        if(isGroup(message.getSender())) {
            String fromUser = message.getSender();
            String m = message.getMessage();
            String query = "SELECT * FROM "+fromUser;
            stmt=conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String toUser = rs.getString("participants");
                int invitationAccepted=rs.getInt("accepted");
                if(invitationAccepted==0)
                    continue;
                if(toUser.equals(message.getReceiver()))
                    continue;
                query = "INSERT INTO messages values (null,?,?,?,?,0,?,?)";
                PreparedStatement stmt2=conn.prepareStatement(query);
                stmt2.setString(1,fromUser);
                stmt2.setString(2,toUser);
                stmt2.setString(3,message.getMessage());
                stmt2.setTimestamp(4,message.getTimestamp());
                stmt2.setInt(5,message.getType());
                stmt2.setString(6,message.getReceiver());
                stmt2.executeUpdate();
//            }
            }
            query="INSERT INTO messages values (null,?,?,?,?,0,?,?)";
            stmt=conn.prepareStatement(query);
            stmt.setString(1,message.getReceiver());
            stmt.setString(2,fromUser);
            stmt.setString(3,message.getMessage());
            stmt.setTimestamp(4,message.getTimestamp());
            stmt.setInt(5,message.getType());
            stmt.setString(6,message.getReceiver());
            stmt.executeUpdate();
            return;
        }
        String q="INSERT INTO messages values (null,?,?,?,?,0,?,?)";
        stmt=conn.prepareStatement(q);
        stmt.setString(1,message.getSender());
        stmt.setString(2,message.getReceiver());
        stmt.setString(3,message.getMessage());
        stmt.setTimestamp(4,message.getTimestamp());
        stmt.setInt(5,message.getType());
        stmt.setString(6,message.getSender());
        stmt.executeUpdate();
    }
    public synchronized void setMessageStatus(Message message) throws SQLException {
        String query="UPDATE messages SET sent=1 WHERE message=? AND messagefrom=? AND messageto=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,message.getMessage());
        stmt.setString(2,message.getSender());
        stmt.setString(3,message.getReceiver());
        stmt.executeUpdate();
    }
    public synchronized boolean isGroup(String groupName) throws SQLException {
        String query="SELECT * FROM users WHERE username=? AND isGroup=1";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,groupName);
        ResultSet rs=stmt.executeQuery();
        if(rs.next())
            return true;
        return false;
    }
    public synchronized void getParticipants(String groupName, List<String> list) throws SQLException {
        String query="SELECT * from "+groupName+" WHERE accepted=1";
        PreparedStatement stmt=conn.prepareStatement(query);
        ResultSet rs=stmt.executeQuery();
        while(rs.next()){
            list.add(rs.getString("participants"));
        }
        return;
    }
    public synchronized String getAdmin(String groupName) throws SQLException {
        String query="SELECT * from "+groupName+" WHERE isAdmin=1";
        PreparedStatement stmt=conn.prepareStatement(query);
        ResultSet rs=stmt.executeQuery();
        while(rs.next()){
            return rs.getString("participants");
        }
        return null;
    }
    public synchronized boolean isAvailable(String username) throws SQLException {
        String query="SELECT * FROM users WHERE username=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,username);
        ResultSet rs=stmt.executeQuery();
        if(rs.next())
            return false;
        else
            return true;
    }
    public synchronized void acceptInvitation(String groupName, String userName) throws SQLException {
        String query="UPDATE "+groupName+" SET accepted=1 WHERE participants=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,userName);
        stmt.executeUpdate();
        query="DELETE FROM invitations WHERE fromUser=? AND toUser=?";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,groupName);
        stmt.setString(2,userName);
        stmt.executeUpdate();
    }
    public synchronized  void deleteGrp(String groupName) throws SQLException {
        String query="Drop table "+groupName;
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.executeUpdate();
        String query1="Delete from users where username =?";
        PreparedStatement stmt1=conn.prepareStatement(query1);
        stmt1.setString(1,groupName);
        stmt1.executeUpdate();
    }
    public synchronized void rejectInvitation(String groupName, String userName) throws SQLException {
        String query="DELETE FROM "+groupName+" WHERE participants=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,userName);
        stmt.executeUpdate();
        query="DELETE FROM invitations WHERE fromUser=? AND toUser=?";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,groupName);
        stmt.setString(2,userName);
        stmt.executeUpdate();
    }
    public synchronized void groupRequest(String groupName, String fromUser) throws SQLException {
        //first check if a request is already present
        String query="SELECT * FROM invitations where fromuser=? AND touser=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,fromUser);
        stmt.setString(2,groupName);
        ResultSet rs=stmt.executeQuery();
        if(rs.next())
            return;
        //if not then insert the request
        query="INSERT INTO invitations values ('"+fromUser+"','"+groupName+"',1)";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,fromUser);
        stmt.setString(2,groupName);
        stmt.executeUpdate();
    }
    public synchronized void acceptRequest(String fromUser, String groupName) throws SQLException {
        String query="INSERT INTO "+groupName+" VALUES (?,0,1)";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,fromUser);
        stmt.executeUpdate();
        query="DELETE FROM invitations WHERE fromuser=? AND touser=?";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,fromUser);
        stmt.setString(2,groupName);
        stmt.executeUpdate();
    }
    public synchronized void rejectRequest(String fromUser, String groupName) throws SQLException {
        String query="DELETE FROM invitations WHERE fromuser=? AND touser=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,fromUser);
        stmt.setString(2,groupName);
        stmt.executeUpdate();
    }
    static int id;
    public synchronized int idSelect(String username,String email) throws SQLException {
        String query="SELECT id FROM users WHERE username=?";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,username);
        ResultSet rs=stmt.executeQuery();
        rs.next();
              int id = rs.getInt("id");
              return id;

    }
    public synchronized void createGroup(String filename,Packet p) throws SQLException {
        String name=p.string1;
        String admin=p.string2;
        String query=null;

        query="INSERT INTO users VALUES (null,?,null,1,null,null,?)";
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1,name);
        stmt.setString(2,filename);
        stmt.executeUpdate();
        query="CREATE TABLE "+name+" (participants VARCHAR(70), isAdmin boolean, accepted boolean)";
        stmt=conn.prepareStatement(query);
        stmt.executeUpdate();
        for (People pp : p.peopleList){
            query="INSERT INTO "+name+" VALUES (?,0,0)";
            stmt=conn.prepareStatement(query);
            stmt.setString(1,pp.getUserName());
            stmt.executeUpdate();
            query="INSERT INTO invitations VALUES (?,?,0)";//filling the invitations table
            stmt=conn.prepareStatement(query);
            stmt.setString(1,name);
            stmt.setString(2,pp.getUserName());
            stmt.executeUpdate();
        }
        query="INSERT INTO "+name+" VALUES (?,1,1)";
        stmt=conn.prepareStatement(query);
        stmt.setString(1,admin);
        stmt.executeUpdate();
        return;
    }
    public synchronized void deleteuser(String username) throws SQLException {
        String query = "Delete from users where username = ?";
        PreparedStatement stmt1 = conn.prepareStatement(query);
        stmt1.setString(1, username);
        stmt1.executeUpdate();
}
}

