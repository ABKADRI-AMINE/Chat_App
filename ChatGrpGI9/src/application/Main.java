package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Main extends Application {
    private static Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private MainController mainController;
    private static People user;
    private ClientSender clientSender;
    private ClientReceivingThread clientReceivingThread;
    private Stage stage;
    private LoginController loginController;
    public static int isConnected;

    @Override
    public void init() throws Exception {
        super.init();
        if(establishConnection()==0)
            System.out.println("Connection problem");
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/fxml/login.fxml"));
        Parent root=fxmlLoader.load();
        loginController=fxmlLoader.<LoginController>getController();
        this.stage=primaryStage;
        primaryStage.setTitle("Login or SignUp ");
        primaryStage.setScene(new Scene(root, 637, 435));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {

            System.out.println("Stage closed");
            try {
                ClientSender.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        primaryStage.show();
    }
    public int establishConnection(){

        try {
            socket=new Socket("127.0.0.1",8888);
            ClientSender.setSocket(socket);
            System.out.println("Connection to server established!!");

            isConnected=1;
        } catch (IOException e) {
            isConnected=0;
            return 0;
        }
        return 1;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        launch(args);
    }

    public static Socket getSocket() {
        return socket;
    }
    public static People getUser(){
        return user;
    }
    public static void setUser(People u){
        user=u;
    }
    public Stage getStage() {
        return stage;
    }
}
