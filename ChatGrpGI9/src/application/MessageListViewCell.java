package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class MessageListViewCell extends ListCell<Message> {
    @FXML
    private Label messageBody;
    @FXML
    private Label messageDate;
    @FXML
    private Label messageTime;
    @FXML
    private VBox outerBody;
    @FXML
    private ImageView fileImageView;
    @FXML
    private ImageView downloadButton;
    @FXML
    private Label actualSender;
    @FXML
    private VBox mainOuterBody;
    private String formattedDate;
    @Override
    public void updateItem(Message message, boolean empty) {
        super.updateItem(message,empty);
        if(message!=null && !empty){
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("../resources/fxml/messagecell.fxml"));
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            formattedDate = new SimpleDateFormat("dd/MM/yyyy   HH:mm").format(message.getTimestamp());
            System.out.println(formattedDate);
            messageDate.setVisible(false);
            messageTime.setVisible(false);
//            mainOuterBody.setStyle("-fx-background-insets:-5;");
            BackgroundImage myBI= new BackgroundImage(new Image("resources/images/cellBG2.jpg",100,100,false,true),
                    BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT);
            //then you set to your node
            mainOuterBody.setBackground(new Background(myBI));

            String mess=message.getMessage();
            if(message.getType()==1) {
                messageBody.setText(message.getMessage().substring(message.getMessage().lastIndexOf("/")+1));
                fileImageView.setFitWidth(100);
                fileImageView.setFitHeight(100);
                Image image;

                if(isImagefile(message.getMessage())
                ){

                    image=new Image("resources/images/images.png");

                }
                else{
                    image = new Image("resources/images/fileAttachement.png");
                }
                fileImageView.setImage(image);
                if(message.getRead()==1){
                    downloadButton.setImage(null);
                    downloadButton.setVisible(false);
                    downloadButton.setFitHeight(0);
                    downloadButton.setFitWidth(0);
                }else{
                    downloadButton.setFitHeight(30);
                    downloadButton.setFitWidth(30);
                    downloadButton.setImage(new Image("resources/images/download.png"));
                }
            }else{
                messageBody.setText(message.getMessage());
                fileImageView.setVisible(false);
                fileImageView.setFitWidth(0);
                fileImageView.setFitHeight(0);
                downloadButton.setVisible(false);
                downloadButton.setFitHeight(0);
                downloadButton.setFitWidth(0);
//                downloadButton.setMaxSize(0,0);
            }
//            messageBody.setText(message.getMessage());
            actualSender.setText(message.getActualSender());
            System.out.println("Sender in message is: "+message.getSender());
            if(message.getSender().equals(Main.getUser().getUserName())) {
                downloadButton.setVisible(false);
                if(message.getType()==0)
                    outerBody.setStyle("-fx-border-color:darkgreen; -fx-background-color: lightgreen;-fx-shape: \"M42.008,0h-24c-9.925,0-18,8.075-18,18v14c0,9.925,8.075,18,18,18h1.78c6.968,0,13.519,2.713,18.446,7.64l1.876,1.877c0.322,0.321,0.746,0.498,1.195,0.498c0.938,0,1.702-0.762,1.702-1.699v-8.344c9.462-0.521,17-8.383,17-17.973V18C60.008,8.075,51.932,0,42.008,0z\"");
                else
                    outerBody.setStyle("-fx-border-color:darkgreen; -fx-background-color: lightgreen;");
                this.setAlignment(Pos.CENTER_RIGHT);
                mainOuterBody.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            }else{
                if(message.getType()==0)
                     outerBody.setStyle("-fx-border-color:darkblue; -fx-background-color: lightblue;-fx-shape: \"M42.008,0h-24c-9.925,0-18,8.075-18,18v14c0,9.59,7.538,17.452,17,17.973v8.344c0,0.937,0.764,1.699,1.702,1.699c0.449,0,0.873-0.177,1.195-0.499l1.876-1.876C26.709,52.713,33.26,50,40.227,50h1.78c9.925,0,18-8.075,18-18V18C60.008,8.075,51.932,0,42.008,0z\"");
                else
                    outerBody.setStyle("-fx-border-color:darkblue; -fx-background-color: lightblue;");
                this.setAlignment(Pos.CENTER_LEFT);
                mainOuterBody.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
            this.setText(null);
            this.setGraphic(mainOuterBody);
        }else{
            this.setText(null);
            this.setGraphic(null);
        }
    }
    public void getFileFromServer() throws IOException {
        System.out.println("Getting file from server");
        downloadButton.setVisible(false);
        Message message=this.getItem();
        if(message.getType()==0)
            return;
        ClientSender.getFile(message);
        this.getItem().setRead(1);
        downloadButton.setFitWidth(0);
        downloadButton.setFitHeight(0);
        downloadButton.setImage(null);
//        FileReceiver.receiveFile()
    }
    public void showDate(){
        messageDate.setVisible(true);
        messageDate.setText(formattedDate);
        messageTime.setText("");
    }
    public void hideDate(){
        messageDate.setVisible(false);
        messageDate.setText("");
        messageTime.setText("");
    }
    private boolean isImagefile(String path){
        String ext=path.substring(path.lastIndexOf('.')+1);
        if(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("png"))
            return true;
//        if(path.equalsIgnoreCase("jpg") || path.equalsIgnoreCase("jpeg") || path.equalsIgnoreCase("png"))
//            return true;
        return false;
    }
}
