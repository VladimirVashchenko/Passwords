package passwordProtector.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import passwordProtector.dataProcessing.DBHelper;
import passwordProtector.Main;
import passwordProtector.StageComposer;
import passwordProtector.dataProcessing.PasswordHash;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;

public class SignUpFormController implements Initializable {
    private Main main;
    @FXML
    private TextField edit_sgnUpLogin;
    @FXML
    private PasswordField edit_sgnUpPass, edit_sgnUpPassConfirm;
    @FXML
    private Button btn_sgnUpOK, btn_sgnUpCancel;

    private static final DBHelper db = DBHelper.getInstance();

    public SignUpFormController(Main main) {
        this.main = main;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_sgnUpOK.setOnAction((event) -> {
            try {
                handleSignUpButtonAction();
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        });
        btn_sgnUpCancel.setOnAction((event) -> {
            try {
                handleSignUpCancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleSignUpButtonAction() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        if (!edit_sgnUpLogin.getText().isEmpty() && !edit_sgnUpPass.getText().isEmpty() && !edit_sgnUpPassConfirm.getText().isEmpty()) {
            if (!db.doesUserExist(edit_sgnUpLogin.getText())) {
                if (edit_sgnUpPass.getText().equals(edit_sgnUpPassConfirm.getText())) {

                    String username = edit_sgnUpLogin.getText();
                    String password = PasswordHash.createHash(edit_sgnUpPass.getText());
                    String salt = PasswordHash.getSalt(); // генерирует соль для пользователя, которая будет использоваться для создания пароля шифрования
                    try {
                        db.insertPublicUser(username, password, salt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    StageComposer.setNotifier("success", null, "Пользователь создан", Duration.millis(1700), 30, 180, 205, null);
                    edit_sgnUpLogin.setText("");
                    edit_sgnUpPass.setText("");
                    edit_sgnUpPassConfirm.setText("");
                } else {
                    StageComposer.setNotifier("warning", null, "Пароли не совпадают", Duration.millis(1700), 30, 180, 205, null);
                }
            } else {
                StageComposer.setNotifier("error", null, "Такой пользователь уже существует", Duration.millis(1700), 30, 200, 205, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Заполните все поля", Duration.millis(1500), 30, 180, 205, null);
        }
    }

    @FXML
    public void handleSignUpCancel() throws IOException {
        main.getPrimaryStage().close();
        main.loadAuthentication();

    }

}
