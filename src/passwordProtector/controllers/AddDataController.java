package passwordProtector.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import passwordProtector.dataProcessing.DBHelper;
import passwordProtector.dataProcessing.KeyWord;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Контроллер панели, через которую вводятся новые записи для хранения в базе данных.
 */
public class AddDataController implements Initializable {
    private DBHelper db;
    private KeyWord keyWord;
    @FXML
    private TextField edit_addLocation, edit_addUsername, edit_addPassword;
    @FXML
    private TextArea txt;
    @FXML
    private Pane pane_addData;
    @FXML
    private Button btn_add, btn_addUser;
    private MainController mainController;

    public AddDataController(MainController mainController) {
        db = DBHelper.getInstance();
        keyWord = KeyWord.getInstance();
        this.mainController = mainController;
    }

    public void setLocation(String treeItem) {
        edit_addLocation.setText(treeItem);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            btn_add/*User*/.setOnAction(this::handleAddData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddData(ActionEvent actionEvent) {
        if (!edit_addLocation.getText().isEmpty() && edit_addUsername.getText().isEmpty() && edit_addPassword.getText().isEmpty()) {
            addLocation();
        } else if (!edit_addLocation.getText().isEmpty() && !edit_addUsername.getText().isEmpty() && !edit_addPassword.getText().isEmpty()) {
            addUser();
        }
    }

    private void addLocation() {
        mainController.addLocationToTree(edit_addLocation.getText());
        edit_addLocation.setText("");
    }

    private void addUser() {
        if (mainController.addUserToTree(edit_addLocation.getText(), edit_addUsername.getText(), edit_addPassword.getText(), txt.getText())) {
            edit_addUsername.setText("");
            edit_addPassword.setText("");
            txt.setText("");
        }
    }
}
