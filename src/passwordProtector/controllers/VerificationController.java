package passwordProtector.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import passwordProtector.dataProcessing.DBHelper;
import passwordProtector.Main;
import passwordProtector.Notification;
import passwordProtector.StageComposer;
import passwordProtector.dataProcessing.KeyWord;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Administrator on 21.04.2016.
 */
public class VerificationController implements Initializable {
    @FXML
    Button btn_verifyOk, btn_verifyCancel, btn_optionsNext, btn_optionsCancel;
    Stage stage, primaryStage;
    @FXML
    TextField edit_verifyLogin;
    @FXML
    PasswordField edit_verifyPass;
    @FXML
    CheckBox check_changeLogin, check_changePass;
    private String oldUsername, oldPassword;
    private MainController mainController;
    private final static DBHelper db = DBHelper.getInstance();

    VerificationController(Stage stage, Stage primaryStage) {
        this.stage = stage;
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_verifyOk.setOnAction(event -> verify(edit_verifyLogin.getText(), edit_verifyPass.getText()));
        btn_verifyCancel.setOnAction(this::cancel);
    }

    public void setMainController(MainController controller) {
        mainController = controller;
    }

    private void verify(String username, String password) {
        Notification.Notifier.setPopupLocation(stage, Pos.BOTTOM_CENTER);

        KeyWord keyWord = KeyWord.getInstance();
        // verifies the user
        if (!username.isEmpty() && !password.isEmpty()) {
            this.oldUsername = username;
            this.oldPassword = password;
            boolean let_in = db.getUserValidation(username, password);
            if (keyWord.getUser().equals(username) && let_in) {
                loadOptionsPanel();
            } else {
                StageComposer.setNotifier("error", null, "Проверьте данные", Duration.millis(1500), 30, 180, stage.getMaxHeight()-60, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Заполните все поля", Duration.millis(1500), 30, 180, stage.getMaxHeight()-60, null);
        }
    }

    private void loadOptionsPanel() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/OptionsDialog.fxml"));
        loader.setController(this);

        AnchorPane rootLayout = null;
        try {
            rootLayout = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StageComposer.setUpStage(stage, rootLayout, "", 320, 320, 200, 200);

        btn_optionsNext.setOnAction(this::options);
        btn_optionsCancel.setOnAction(this::cancel);
    }

    private void options(ActionEvent event) {
        boolean uSelected = check_changeLogin.isSelected();
        boolean pSelected = check_changePass.isSelected();

        if (!pSelected && uSelected) {
            loadChangePanel('u');
        } else if (!uSelected && pSelected) {
            loadChangePanel('p');
        } else if (uSelected && pSelected) {
            loadChangePanel('b');
        } else {
            StageComposer.setNotifier("warning", null, "Сделайте выбор!", Duration.millis(2000), 30, 230, stage.getMaxHeight()-60, null);
        }
    }

    private void loadChangePanel(char choice) {
        DataChangeController dcc = new DataChangeController(stage, primaryStage, oldUsername, oldPassword, choice);
        FXMLLoader loader = new FXMLLoader();
        String fxml;
        String title;
        int height;

        switch (choice) {
            case 'u':
                fxml = "view/ChangeUsernameDialog.fxml";
                title = "Изменение логина";
                height = 180;
                break;
            case 'p':
                fxml = "view/ChangePassDialog.fxml";
                title = "Изменение пароля";
                height = 210;
                break;
            case 'b':
                fxml = "view/SignUpDialog.fxml";
                title = "Изменение данных";
                height = 250;
                break;
            default:
                throw new IllegalArgumentException();
        }

        loader.setLocation(Main.class.getResource(fxml));
        loader.setController(dcc);
        AnchorPane rootLayout = null;
        try {
            rootLayout = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StageComposer.setUpStage(stage, rootLayout, title, 320, 320, height, height);
    }

    private void cancel(ActionEvent actionEvent) {
        Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);
        stage.close();
    }
}
