package application;

import Server.PreparedDatabaseProxy;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;


public class UpdateController {

    private static PreparedDatabaseProxy db;

    @FXML
    private ImageView profilePhotoImageView;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField passwordCheckTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField contactTextField;
    private File file;
    private String username;
    private String password;
    private String email;
    private String contact;
    private boolean passwordCheck=false;
    private boolean emailCheck=false;
    private static boolean usernameCheck=false;
    private static TextField staticUserName;
    @FXML
    public void initialize(){
        staticUserName=usernameTextField;
        Rectangle rect=new Rectangle(profilePhotoImageView.getFitWidth(),profilePhotoImageView.getFitHeight());
        rect.setArcHeight(200);
        rect.setArcWidth(200);
        profilePhotoImageView.setClip(rect);
    }
    public void uploadProfilePhoto1() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files","*.jpg","*.jpeg","*.png","*.jfif"));
        fileChooser.setTitle("Upload profile photo");
        file=fileChooser.showOpenDialog(profilePhotoImageView.getScene().getWindow());
        if(file!=null){
            System.out.println("File path:"+file.getPath());
            profilePhotoImageView.setImage(new Image(file.toURI().toURL().toExternalForm()));
        }
    }


    public void update() throws IOException, SQLException {
        PeopleListViewCell p1 = new PeopleListViewCell();
        People p = p1.getItem();
        String username1= Main.getUser().getUserName();
        System.out.println(username1);
        username=usernameTextField.getText();
        contact=contactTextField.getText();
        if(username.isEmpty() || password.isEmpty() || email.isEmpty() || contact.isEmpty() || !passwordCheck || !emailCheck || !usernameCheck) {
            DialogBox.showErrorDialog("One or more fields are incorrect or empty");
            return;
        }
        //db.idSelect(p.getUserName(),p.getEmail());
        String hashedPassword= Hash.toSHA1(password);
        ClientSender.update(file,username,hashedPassword,email,contact,username1);
        DialogBox.informationDialog("Update successful");
        Stage st=(Stage) passwordTextField.getScene().getWindow();
        st.close();
    }
    public void checkAvailability1() throws IOException {
//        System.out.println(usernameTextField);
        username=usernameTextField.getText();
        if(username.isEmpty())
            return;
        if(username.contains(" ")) {
            setAvailability1(false);
            return;
        }
        ClientSender.checkAvailability1(username,0);
    }
    public static void setAvailability1(boolean available){
        usernameCheck=available;
        if(available)
            staticUserName.setStyle("-fx-text-inner-color : green;");
        else
            staticUserName.setStyle("-fx-text-inner-color : red;");
    }
    public void checkPasswordCorrectness1(){
        password=passwordTextField.getText();
        String pp=passwordCheckTextField.getText();
        if(!pp.equals(password)) {
            passwordCheckTextField.setStyle("-fx-text-inner-color : red;");
            passwordCheck=false;
        }
        else {
            passwordCheckTextField.setStyle("-fx-text-inner-color : green;");
            passwordCheck=true;
        }
    }
    public void verifyEmail1(){
        email=emailTextField.getText();
        if(email.contains("@") && email.contains(".") && email.indexOf("@")==email.lastIndexOf("@")) {
            emailTextField.setStyle("-fx-text-inner-color : green;");
            emailCheck=true;
        }
        else {
            emailTextField.setStyle("-fx-text-inner-color : red;");
            emailCheck=false;
        }
    }
}
