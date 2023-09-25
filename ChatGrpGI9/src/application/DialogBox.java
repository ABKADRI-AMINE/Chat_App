package application;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class DialogBox {
    public static void showErrorDialog(String text){
        Alert alert=new Alert(Alert.AlertType.ERROR,text, ButtonType.OK);
        alert.showAndWait();
    }
    public static void informationDialog(String text){
        Alert alert=new Alert(Alert.AlertType.INFORMATION,text,ButtonType.OK);
        alert.showAndWait();
    }
}
