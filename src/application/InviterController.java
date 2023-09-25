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

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;


public class InviterController {

    private static PreparedDatabaseProxy db;


    @FXML
    private TextField emailTextField;
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
        //staticUserName=usernameTextField;
        //Rectangle rect=new Rectangle(profilePhotoImageView.getFitWidth(),profilePhotoImageView.getFitHeight());
        //rect.setArcHeight(200);
        //rect.setArcWidth(200);
        //profilePhotoImageView.setClip(rect);
    }


    public void inviter() throws IOException, SQLException {

        try{
            String emailT = emailTextField.getText();
            Properties properties = new Properties();
            properties.put("mail.smtp.auth",true);
            properties.put("mail.smtp.host","smtp.gmail.com");
            properties.put("mail.smtp.port",587);
            properties.put("mail.smtp.starttls.enable",true);
            properties.put("mail.transport.protocol","smtp");
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                //tma l'email mnin ansift l'email
                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication("youssefabidi929@gmail.com","uktrlkixftruwsrq");
                }
            });


            javax.mail.Message message = new MimeMessage(session);
            message.setSubject("Invitation à rejoundre ChatGi");
            //destination
            Address addressTo = new InternetAddress(emailT);
            message.setRecipient(javax.mail.Message.RecipientType.TO,addressTo);
            //anonymous message
            message.setRecipient(Message.RecipientType.CC,addressTo);
            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart messageBodyPart =new MimeBodyPart();
            messageBodyPart.setContent("<h1>Vous êtes inviter par "+Main.getUser().getUserName()+" à rejoundre notre application ChatGi pour communiquer avec vos camarades !</h1>","text/html");
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception e) {
            // TODO: handle exception

        }
        Stage stage = (Stage) emailTextField.getScene().getWindow();

        stage.close();

    }

    public void verifyEmail2(){
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
