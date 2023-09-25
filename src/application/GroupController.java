package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class GroupController {
    @FXML
    private ListView<People> groupPeopleListView;
    @FXML
    private TextField groupName;
    @FXML
    private ImageView groupProfileImageView;
    private File file;
    private static TextField staticUserName;
    private static boolean usernameAvailability;
    private MainController mainController;
    @FXML
    public void initialize(){
        file=null;
        groupPeopleListView.setCellFactory(groupPeopleListView -> new PeopleListViewCell());
        groupPeopleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Rectangle rect=new Rectangle(groupProfileImageView.getFitWidth(),groupProfileImageView.getFitHeight());
        rect.setArcHeight(150);
        rect.setArcWidth(150);
        groupProfileImageView.setClip(rect);
        staticUserName=groupName;
    }
    public void setMainController(MainController mainController){
        this.mainController=mainController;
    }
    public void setGroupPeopleListView(ObservableList<People> list){
        ObservableList<People> updated= FXCollections.observableArrayList();
        for (People pp : list){
            if(pp.getType()==0)
                updated.add(pp);
        }
        groupPeopleListView.setItems(updated);
    }
    public void createGroup() throws IOException {
        String name=groupName.getText();
        ObservableList<People> participants=groupPeopleListView.getSelectionModel().getSelectedItems();
        if(name.isEmpty() || participants.isEmpty() || !usernameAvailability) {
            DialogBox.showErrorDialog("One or more fields are incorrect or empty!!");
            return;
        }
        ClientSender.createGroup(name,participants,file);
        Stage stage=(Stage) groupName.getScene().getWindow();
        stage.close();
//        mainController.initializePeople();
    }
    public void uploadProfilePhoto() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files","*.jpg","*.jpeg","*.png","*.jfif"));
        fileChooser.setTitle("Upload group photo");
        file=fileChooser.showOpenDialog(groupProfileImageView.getScene().getWindow());
        if(file!=null){
            System.out.println("File path:"+file.getPath());
            groupProfileImageView.setImage(new Image(file.toURI().toURL().toExternalForm()));
        }
    }

    public void checkGroupAvailability() throws IOException {
        String username=groupName.getText();
        if(username.isEmpty())
            return;
        if(username.contains(" ")){
            setAvailability(false);
            return;
        }
        ClientSender.checkAvailability(username,1);
    }

    public static void setAvailability(boolean available){
        usernameAvailability=available;
        if(available)
            staticUserName.setStyle("-fx-text-inner-color : green;");
        else
            staticUserName.setStyle("-fx-text-inner-color : red;");
    }
    public static void setAvailability1(boolean available){
        usernameAvailability=available;
        if(available)
            staticUserName.setStyle("-fx-text-inner-color : green;");
        else
            staticUserName.setStyle("-fx-text-inner-color : red;");
    }
}
