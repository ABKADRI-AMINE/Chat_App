package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;


public class LoginController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    private Socket socket;
    private static TextField staticField;
    public LoginController(){
        socket=Main.getSocket();
    }
    @FXML
    public void initialize() throws IOException {
        System.out.println("Initializing login controller");
        staticField=usernameTextField;
        ClientReceivingThread clientReceivingThread=new ClientReceivingThread(socket);
        Thread t=new Thread(clientReceivingThread);
        t.start();
    }
    public void login() throws IOException {
        String username=usernameTextField.getText();
        String password=passwordTextField.getText();
        System.out.println("Username : "+username);
        System.out.println("Password : "+password);
        if(username.isEmpty() || password.isEmpty()) {
            DialogBox.showErrorDialog("All fields are necessary!");
            return;
        }
        String hashedPassword= Hash.toSHA1(password);
        ClientSender.login(username,hashedPassword);
    }
    public void signUp() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/signup.fxml"));
        Parent root=fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Welcome");
        stage.setScene(new Scene(root, 600, 400));
        stage.setResizable(false);
        stage.showAndWait();
    }
    public static void loginResponse(boolean response){
        if(response){
            ((Stage)staticField.getScene().getWindow()).close();
        }else{
            DialogBox.showErrorDialog("Login failed");
        }
    }
}
