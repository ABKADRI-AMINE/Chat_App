package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;


public class ProfileController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label contactLabel;
    @FXML
    private ImageView imageView;
    @FXML
    public void initialize(){
        Rectangle rect=new Rectangle(imageView.getFitWidth(),imageView.getFitHeight());
        rect.setArcHeight(150);
        rect.setArcWidth(150);
        imageView.setClip(rect);
    }



    public void setInfo(String username, String email, String contact,byte[] buff){
        usernameLabel.setText(username);
        emailLabel.setText(email);
        contactLabel.setText(contact);
        if(buff!=null)
           imageView.setImage(new Image(new ByteArrayInputStream(buff)));
    }
}
