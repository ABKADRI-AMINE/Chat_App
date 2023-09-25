package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PeopleListViewCell extends ListCell<People> {

    @FXML
    private Label userName;
    @FXML
    private HBox outerBody;
    @FXML
    private Circle onlineCircle;
    @FXML
    private ImageView profilePhotoImageView;
    @FXML
    private Label unreadMessageCount;
    @Override
    public void updateItem(People people, boolean empty) {
        super.updateItem(people,empty);
        if(people!=null){
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("../resources/fxml/peoplecell.fxml"));
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Rectangle rect=new Rectangle(profilePhotoImageView.getFitWidth(),profilePhotoImageView.getFitHeight());
            rect.setArcHeight(34);
            rect.setArcWidth(34);
            profilePhotoImageView.setClip(rect);
            userName.setText(people.getUserName());
            unreadMessageCount.setVisible(true);
            unreadMessageCount.setText(""+people.getUnreadMessageCount());
            if(people.getUnreadMessageCount()==0)
                unreadMessageCount.setVisible(false);
            if(people.getBuff()!=null) {
                Image im=new Image(new ByteArrayInputStream(people.getBuff()));
                profilePhotoImageView.setImage(im);
            }
            if(people.getOnlineStatus()==1)
                onlineCircle.setStrokeWidth(3);
            else
                onlineCircle.setStrokeWidth(0);
            this.setAlignment(Pos.CENTER_LEFT);
            setText(null);
            setGraphic(outerBody);

        }else{
            setGraphic(null);
            setText(null);
//            System.out.println("Null entry to people list");
        }
    }

    public void openProfile() throws IOException, SQLException {
        System.out.println("Opening profile");
        People p=this.getItem();
        if(p.getType()==1){//It is a group
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
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/profile.fxml"));
        Parent root=fxmlLoader.load();
        ProfileController profileController=fxmlLoader.<ProfileController>getController();
        profileController.setInfo(p.getUserName(),p.getEmail(),p.getContact(),p.getBuff());
        Stage stage=new Stage();
        stage.setTitle("User Profile");
        stage.setScene(new Scene(root, 406, 231));
        stage.setResizable(false);
        stage.showAndWait();
    }
    public void removeCount(){
        unreadMessageCount.setVisible(false);
    }
}
