package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


public class MainController {
    @FXML
    private ListView<Message> messageListView;
    @FXML
//    private JFXListView<People> peopleListView;
    private ListView<People> peopleListView;
    @FXML
    private ComboBox<Notification> notificationsComboBox;
    @FXML
    private Label notificationsCount;
    @FXML
    private TextField messageTextField;
    @FXML
    private TextField searchTextField;
//    @FXML
//    private ListView<People> groupPeopleListView;
//    @FXML
//    private Button uploadButton;
    private ObservableList<Message> messageList;
    private ObservableList<People> peopleList;
    private ObservableList<Notification> notificationList;
//    private List<String> stringList=new ArrayList<>(5);
    private int sendFile;
    private Main main;
    private Socket socket;
    private ClientSender clientSender;
    private People currentlyOpenUser;
    public MainController(){
        socket=Main.getSocket();
    }
    @FXML
    public void initialize() throws IOException {
        System.out.println("Initializing messages list");
        messageList= FXCollections.observableArrayList();
        messageListView.setItems(this.messageList);
        messageListView.setCellFactory(messageListView -> new MessageListViewCell());
        peopleList=FXCollections.observableArrayList();
        peopleListView.setItems(this.peopleList);
        peopleListView.setCellFactory(peopleListView -> new PeopleListViewCell());

        notificationsComboBox.promptTextProperty().setValue("Notifications");

        notificationList=FXCollections.observableArrayList();
        notificationsComboBox.setItems(this.notificationList);
        notificationList.addListener(new ListChangeListener<Notification>() {
            @Override
            public void onChanged(Change<? extends Notification> c) {
                notificationsCount.setText(""+notificationList.size());
            }
        });
//        initializePeople();
//        initializeNotifications();

        //for enabling searching
        FilteredList<People> filteredData = new FilteredList<>(peopleList, p -> true);
        //Set the filter Predicate whenever the filter changes.
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(client ->{
                // If filter text is empty, display all persons.
                if(newValue == null || newValue.isEmpty()){
                    return true;
                }

                // Compare username of every client with filter text.
                String userNameFilter = newValue.toLowerCase();

                if(client.getUserName().contains(userNameFilter)){
                    return true; //filter matches first name
                }
                return false; //Does not match
            });
        });

        //Wrap the FilteredList in a SortedList.
        SortedList<People> sortedData = new SortedList<>(filteredData);

        //put the sorted list into the listview
        peopleListView.setItems(sortedData);
    }
    public void myInitializations() throws IOException {
        initializeNotifications();
        initializePeople();
    }
    @FXML
    public void sendMessage() throws IOException {
        String text=messageTextField.getText();
        messageTextField.setText("");
        if(text.isEmpty() && sendFile!=1)//do nothing
            return;
        int userType=currentlyOpenUser.getType();
        Message message;
        String fromUser;
        Message message2;
        if(userType==0)
            message=new Message(text,new Timestamp(new Date().getTime()),Main.getUser().getUserName(),currentlyOpenUser.getUserName(),0,0,Main.getUser().getUserName());
        else {
            message = new Message(text, new Timestamp(new Date().getTime()),currentlyOpenUser.getUserName(),Main.getUser().getUserName(), 0, 0,Main.getUser().getUserName());
        }
        message2=new Message(text, new Timestamp(new Date().getTime()),Main.getUser().getUserName(),currentlyOpenUser.getUserName(), 0, 0,Main.getUser().getUserName());
        messageList.add(message2);
        ClientSender.sendMessage(message);
    }
    @FXML
    public void pressedEnter(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode()== KeyCode.ENTER)
            sendMessage();
    }
    public void initializeNotifications() throws IOException {//as notifications
        System.out.println("Initializing notifications");
        ClientSender.notificationsList();
    }
    public void setNotificationList(List<Notification> notificationList){
        System.out.println("Notifications="+notificationList.size());
        Platform.runLater(()->{
//            notificationsCount.setText(""+notificationList.size());
            for (Notification nn : notificationList) {
                System.out.println(nn);
                this.notificationList.add(nn);
                if(currentlyOpenUser!=null) {
                    if (nn.getSender().equals(currentlyOpenUser.getUserName())) {
                        this.messageList.add(nn.getMessage());
                    }
                }
            }
        });
    }

    public void initializePeople() throws IOException {
        System.out.println("Initializing people list");
        ClientSender.peopleList();
    }
    public void setPeopleList(List<People> peopleList){
        Platform.runLater(() -> {
            for(People pp : peopleList){
                if(pp.getUserName().equals(Main.getUser().getUserName()))
                    continue;
                if(this.peopleList.contains(pp)){
                    System.out.println("PEOPLE FOUND");
                    int ind=this.peopleList.indexOf(pp);
                    this.peopleList.remove(ind);
//                    this.peopleList.get(ind).setOnlineStatus(1);
                    System.out.println("Updating online status of: "+pp.getUserName());
                }
//                pp.setUnreadMessageCount(Collections.frequency(notificationList,new Notification(pp.getUserName(),Main.getUser().getUserName(),,2)));
                int cnt=0;
                for(Notification nn : notificationList){
                    if(nn.getSender().equals(pp.getUserName()))
                        cnt++;
                }

                pp.setUnreadMessageCount(cnt);
                if(pp.getOnlineStatus()==1)
                    this.peopleList.add(0,pp);
                else
                    this.peopleList.add(pp);
//                peopleListView.refresh();
            }
        });

        System.out.println(""+peopleList.size());
        for (People pp : peopleList)
            System.out.println(pp.getUserName());
    }
    public void addPeople(People people){
        Platform.runLater(()-> {
            peopleList.add(0, people);
        });
    }
    public void changeOnlineStatus(People people){
        Platform.runLater(()-> {
//            int ind = peopleList.indexOf(people);
            System.out.println("changing online status of : "+people.getUserName());
            peopleList.remove(people);
            people.setOnlineStatus(0);
            peopleList.add(people);
        });
    }
    public void updatePeopleList(String groupName, String newUser){
        Platform.runLater(()-> {
            for (People pp : peopleList) {
                if (pp.getUserName().equals(groupName)) {
                    pp.addParticipant(newUser);
                    break;
                }
            }
        });
    }
    public void fetchMessages() throws IOException {
        messageListView.getItems().clear();
        currentlyOpenUser=peopleListView.getSelectionModel().getSelectedItem();
        if(currentlyOpenUser==null)
            return;
        System.out.println("Opening user: "+currentlyOpenUser);

        if (currentlyOpenUser.getType()==1 && !currentlyOpenUser.isParticipant(Main.getUser().getUserName())){
//            DialogBox.showErrorDialog("Oops! You are not a participant of this group.");
            Alert alert=new Alert(Alert.AlertType.ERROR,"Oops! You are not a participant of this group.\nDo you want to send request?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if(alert.getResult()==ButtonType.YES){
                ClientSender.sendGroupRequest(currentlyOpenUser.getUserName());
            }
            return;
        }

        ClientSender.fetchMessages(currentlyOpenUser.getUserName());
    }
    public void setMessageList(List<Message> messageList){//not being called on application thread
        Platform.runLater(()->{
            for(Message mm : messageList)
                this.messageList.add(mm);
            messageListView.scrollTo(messageList.size()-1);
        });
    }
    public void setMain(Main main){
        this.main=main;
    }

    public void openFile() throws IOException {
//        Desktop.getDesktop().open();
        Message message=messageListView.getSelectionModel().getSelectedItem();
        if(message==null)
            return;
        if(message.getType()==0 || message.getRead()==0)
            return;
        File file=new File(message.getMessage());
        Desktop.getDesktop().open(file);
    }
    public void handleNotification() throws IOException, SQLException {
        System.out.println("handling notifications!!");
        Notification n=notificationsComboBox.getValue();
        System.out.println("handling notification :" +n);
        if(n==null) {
            System.out.println("Notification is null");
            return;
        }
        if(n.getType()==0){
            //create an invitation acceptance box
            ButtonType groupInfo=new ButtonType("Info");
            Alert alert=new Alert(Alert.AlertType.INFORMATION,"Accept invitation?",ButtonType.YES,ButtonType.NO,groupInfo);
            alert.showAndWait();
            if(alert.getResult()==ButtonType.YES){
                ClientSender.acceptInvitation(n.getSender());


                System.out.println("Removing notification : "+n);

            }
            if(alert.getResult()==ButtonType.NO){
                ClientSender.rejectInvitation(n.getSender());
                notificationsComboBox.getSelectionModel().clearSelection();
                System.out.println("Removing notification : "+n);

            }
            if(alert.getResult()==groupInfo){
                int ind=peopleList.indexOf(new People(n.getSender(),0,0,null,null,null));
                People p=peopleList.get(ind);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/groupprofile.fxml"));
                Parent root=fxmlLoader.load();
                GroupProfileController profileController=fxmlLoader.<GroupProfileController>getController();
                ObservableList<String> participants= FXCollections.observableArrayList();
                List<String> ll=p.getParticipants();
                for (String ss : ll){
                    participants.add(ss);
                }
                profileController.setInfo(p.getUserName(),p.getAdmin(),participants,p.getBuff());
                Stage stage=new Stage();
                stage.setTitle("Group Profile");
                stage.setScene(new Scene(root, 406, 231));
                stage.setResizable(false);
                stage.showAndWait();
                handleNotification();
//                return;
            }
        } else if(n.getType()==1){
            //create a request acceptance dialog box
            ButtonType groupInfo=new ButtonType("Info");
            Alert alert=new Alert(Alert.AlertType.INFORMATION,"Accept request?",ButtonType.YES,ButtonType.NO,groupInfo);
            alert.showAndWait();
            if(alert.getResult()==ButtonType.YES){
                ClientSender.acceptRequest(n.getSender(),n.getReceiver());
//                initializePeople();
//                notificationsComboBox.getSelectionModel().clearSelection();
//                notificationList.remove(n);
            }
            if(alert.getResult()==ButtonType.NO){
                ClientSender.rejectRequest(n.getSender(),n.getReceiver());
//                notificationList.remove(n);
            }
            if(alert.getResult()==groupInfo){
                int ind=peopleList.indexOf(new People(n.getSender(),0,0,null,null,null));
                People p=peopleList.get(ind);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/profile.fxml"));
                Parent root=fxmlLoader.load();
                ProfileController profileController=fxmlLoader.<ProfileController>getController();
                profileController.setInfo(p.getUserName(),p.getEmail(),p.getContact(),p.getBuff());
                Stage stage=new Stage();
                stage.setTitle("User Profile");
                stage.setScene(new Scene(root, 406, 231));
                stage.setResizable(false);
                stage.showAndWait();
                handleNotification();
//                return;
            }
        }
//        ClientSender.notificationsList();
        notificationsComboBox.getSelectionModel().clearSelection();
        notificationsComboBox.getItems().remove(n);
//        notificationList.remove(n);
    }
    public void insertEmoji(){
        byte[] emojiBytes=new byte[]{(byte)0xF0,(byte)0x9F,(byte)0x98,(byte)0x81};
        String emoji=new String(emojiBytes, Charset.forName("UTF-8"));
        messageTextField.setText(emoji);
    }
    public void createGroup() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/creategroup.fxml"));
//        fxmlLoader.setController(this);
        Parent root=fxmlLoader.load();
        GroupController gc=fxmlLoader.<GroupController>getController();
        Stage stage = new Stage();
        stage.setTitle("New Group");
        stage.setScene(new Scene(root));
        gc.setGroupPeopleListView(peopleList);
        gc.setMainController(this);
//        groupPeopleListView.setItems(this.peopleList);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
//        stage.show();
    }

    public void updateProfile() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/update.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Update");
        stage.setScene(new Scene(root, 900, 600));
        stage.setResizable(false);
        stage.showAndWait();
    }
    public void openChooser() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file=fileChooser.showOpenDialog(notificationsCount.getScene().getWindow());//any node can be used in place of notificationsCount
        if(file!=null) {
//            Desktop.getDesktop().open(file); //to open the file
            String absolutePath=file.getAbsolutePath();
            String ext=absolutePath.substring(absolutePath.lastIndexOf(".")+1);
            System.out.println(ext);
            if(currentlyOpenUser!=null) {
                sendFile=1;
                Message m;
                Message m2=new Message(ext, new Timestamp(new Date().getTime()),Main.getUser().getUserName(),currentlyOpenUser.getUserName(),1,1,Main.getUser().getUserName());
                if(currentlyOpenUser.getType()==0)
                    m=new Message(ext+" file", new Timestamp(new Date().getTime()),Main.getUser().getUserName(),currentlyOpenUser.getUserName(),0,1,Main.getUser().getUserName());
                else//group message
                    m=new Message(ext+" file", new Timestamp(new Date().getTime()),currentlyOpenUser.getUserName(),Main.getUser().getUserName(),0,1,Main.getUser().getUserName());
                messageList.add(m2);
                ClientSender.sendFile(absolutePath,m);//send file
                System.out.println("File sent");
            }
        }
    }

    public void inviter() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/inviter.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("inviter");
        stage.setScene(new Scene(root, 900, 600));
        stage.setResizable(false);
        stage.showAndWait();
    }
    public void deleteuser() throws IOException{
        PeopleListViewCell p1 = new PeopleListViewCell();
        People p = p1.getItem();
        String username1= Main.getUser().getUserName();
        System.out.println(username1);
        ClientSender.deleteuser(username1);
        DialogBox.informationDialog("delete successful");
        Platform.exit();
    }
    @FXML
    public void logout(MouseEvent mouseEvent) throws IOException {
        Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        currentStage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/signup.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Welcome");
        stage.setScene(new Scene(root, 600, 400));
        stage.setResizable(false);
        stage.showAndWait();
    }



}

