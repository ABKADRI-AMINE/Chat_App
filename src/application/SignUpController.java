package application;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class SignUpController {
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
    public void uploadProfilePhoto() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files","*.jpg","*.jpeg","*.png","*.jfif"));
        fileChooser.setTitle("Upload profile photo");
        file=fileChooser.showOpenDialog(profilePhotoImageView.getScene().getWindow());
        if(file!=null){
            System.out.println("File path:"+file.getPath());
            profilePhotoImageView.setImage(new Image(file.toURI().toURL().toExternalForm()));
        }
    }
    public void signup() throws IOException {
        username=usernameTextField.getText();
        contact=contactTextField.getText();
        if(username.isEmpty() || password.isEmpty() || email.isEmpty() || contact.isEmpty() || !passwordCheck || !emailCheck || !usernameCheck) {
            DialogBox.showErrorDialog("One or more fields are incorrect or empty");
            return;
        }
        String hashedPassword= Hash.toSHA1(password);
        ClientSender.signup(file,username,hashedPassword,email,contact);
        DialogBox.informationDialog("SignUp successful");
        Stage st=(Stage) passwordTextField.getScene().getWindow();
        st.close();
    }
    public void checkAvailability() throws IOException {
//        System.out.println(usernameTextField);
        username=usernameTextField.getText();
        if(username.isEmpty())
            return;
        if(username.contains(" ")) {
            setAvailability(false);
            return;
        }
        ClientSender.checkAvailability(username,0);
    }
    public static void setAvailability(boolean available){
        usernameCheck=available;
        if(available)
            staticUserName.setStyle("-fx-text-inner-color : green;");
        else
            staticUserName.setStyle("-fx-text-inner-color : red;");
    }
    public void checkPasswordCorrectness(){
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
    public void verifyEmail(){
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
