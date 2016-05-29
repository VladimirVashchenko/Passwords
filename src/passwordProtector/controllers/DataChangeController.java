package passwordProtector.controllers;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import passwordProtector.Main;
import passwordProtector.dataProcessing.*;
import passwordProtector.Helper;
import passwordProtector.Notification;
import passwordProtector.StageComposer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Created by Administrator on 21.04.2016.
 */

public class DataChangeController implements Initializable {
    private char choice;
    @FXML
    private TextField edit_sgnUpLogin;
    @FXML
    private PasswordField edit_sgnUpPass, edit_sgnUpPassConfirm;
    @FXML
    private Button btn_sgnUpOK, btn_sgnUpCancel, btn_progressFinish;
    @FXML
    private Label lbl_progressValue;
    @FXML
    private ProgressBar progress_bar;

    private DBHelper db = DBHelper.getInstance();
    private KeyWord keyWord = KeyWord.getInstance();
    private FXMLLoader loader;
    private Stage stage, primaryStage;
    private File tempFolder;
    private Dispatcher dispatcher = Dispatcher.getInstance();
    private MyTask arrayListTask;
    private ArrayList<DBUser> usersList;
    private String oldUsername, oldPassword, newUsername, newPassword, oldKey, newSalt, path, pathTemp, tempDbName;

    DataChangeController(Stage stage, Stage primaryStage, String oldUsername, String oldPassword, char choice) {
        this.stage = stage;
        this.primaryStage = primaryStage;
        this.oldUsername = oldUsername;
        this.oldPassword = oldPassword;
        this.oldKey = keyWord.getKey();
        this.newSalt = PasswordHash.getSalt();
        this.choice = choice;

        this.usersList = db.getAllUserData();

        path = Helper.filePath() + "UserData/";
        pathTemp = path + "temp/";
        tempFolder = new File(pathTemp);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_sgnUpOK.setOnAction(this::setOKBtn);
        btn_sgnUpCancel.setOnAction(this::cancel);
    }

    private void changeUsernameAndPassword() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (!edit_sgnUpLogin.getText().isEmpty() && !edit_sgnUpPass.getText().isEmpty() && !edit_sgnUpPassConfirm.getText().isEmpty()) {
            if (!db.doesUserExist(edit_sgnUpLogin.getText())) {
                if (edit_sgnUpPass.getText().equals(edit_sgnUpPassConfirm.getText())) {

                    //новые имя пользователя, хэш-пароль
                    newUsername = edit_sgnUpLogin.getText();
                    newPassword = edit_sgnUpPass.getText();
                    final String newKey = keyWord.makeSecretKey(newUsername, newPassword, newSalt);
                    final String newPasswordHashed = PasswordHash.createHash(newPassword);


                    tempDbName = newUsername;

                    arrayListTask = new MyTask() {
                        @Override
                        protected ArrayList<DBUser> call() throws Exception {
                            if (usersList.size() > 0) {
                                copyToTemp(oldUsername, newUsername);
                                common(newKey, newUsername);
                            }

                            btn_progressFinish.setOnAction(actionEvent -> {
                                if (usersList.size() > 0) {
                                    finish(newKey);
                                }
                                keyWord.setUser(newUsername);

                                db.changePublicUserData(oldUsername, newUsername, newPasswordHashed, newSalt);

                                Helper.deleteFile(path, oldUsername);
                                reconnect(newUsername);

                                stage.close();

                                Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);
                                StageComposer.setNotifier("success", null, "Данные изменены", Duration.millis(1700), 30, 180, 15, null);
                            });
                            return null;
                        }

                    };

                    showProgress();

                    dispatcher.getEventExService().execute(arrayListTask);
                } else {
                    StageComposer.setNotifier("warning", null, "Пароли не совпадают", Duration.millis(1700), 30, 180, stage.getMaxHeight() - 60, null);
                }
            } else {
                StageComposer.setNotifier("error", null, "Такой пользователь уже существует", Duration.millis(1700), 30, 200, stage.getMaxHeight() - 60, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Заполните все поля", Duration.millis(1500), 30, 180, stage.getMaxHeight() - 60, null);
        }
    }

    private void changeUsername() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (!edit_sgnUpLogin.getText().isEmpty()) {
            if (!db.doesUserExist(edit_sgnUpLogin.getText())) {

                newUsername = edit_sgnUpLogin.getText();

                //новое имя пользователя
                newUsername = edit_sgnUpLogin.getText();
                final String newKey = keyWord.makeSecretKey(newUsername, oldPassword, newSalt);

                tempDbName = newUsername;

                arrayListTask = new MyTask() {
                    @Override
                    protected ArrayList<DBUser> call() throws Exception {
                        if (usersList.size() > 0) {
                            copyToTemp(oldUsername, newUsername);
                            common(newKey, newUsername);
                        }

                        btn_progressFinish.setOnAction(actionEvent -> {
                            if (usersList.size() > 0) {
                                finish(newKey);
                            }
                            keyWord.setUser(newUsername);

                            db.changePublicUsername(oldUsername, newUsername, newSalt);

                            Helper.deleteFile(path, oldUsername);
                            reconnect(newUsername);
                            stage.close();

                            Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);
                            StageComposer.setNotifier("success", null, "Логин изменён", Duration.millis(1700), 30, 180, 15, null);
                        });
                        return null;
                    }
                };

                showProgress();

                dispatcher.getEventExService().execute(arrayListTask);
            } else {
                StageComposer.setNotifier("error", null, "Такой пользователь уже существует", Duration.millis(1700), 30, 200, stage.getMaxHeight() - 60, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Введите новое имя пользователя", Duration.millis(1500), 30, 180, stage.getMaxHeight() - 60, null);
        }
    }

    private void changePassword() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (!edit_sgnUpPass.getText().isEmpty() && !edit_sgnUpPassConfirm.getText().isEmpty()) {
            if (edit_sgnUpPass.getText().equals(edit_sgnUpPassConfirm.getText())) {

                newPassword = edit_sgnUpPass.getText();

                final String newKey = keyWord.makeSecretKey(oldUsername, newPassword, newSalt);
                final String newPasswordHashed = PasswordHash.createHash(newPassword);


                tempDbName = oldUsername;

                arrayListTask = new MyTask() {
                    @Override
                    protected ArrayList<DBUser> call() throws Exception {
                        if (usersList.size() > 0) {
                            copyToTemp(oldUsername, oldUsername);
                            common(newKey, oldUsername);
                        }

                        btn_progressFinish.setOnAction(actionEvent -> {
                            if (usersList.size() > 0) {
                                finish(newKey);
                            }

                            db.changePublicPassword(oldUsername, newPasswordHashed, newSalt);

                            reconnect(oldUsername);
                            stage.close();

                            Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);
                            StageComposer.setNotifier("success", null, "Пароль изменён", Duration.millis(1700), 30, 180, 15, null);
                        });

                        return null;
                    }
                };

                showProgress();

                dispatcher.getEventExService().execute(arrayListTask);
            } else {
                StageComposer.setNotifier("warning", null, "Пароли не совпадают", Duration.millis(1700), 30, 180, stage.getMaxHeight() - 60, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Заполните все поля", Duration.millis(1500), 30, 180, stage.getMaxHeight() - 60, null);
        }
    }

    /**
     * Выполняет перешифровку
     */
    private void common(String newKey, String tempDbName) {
        db.setDbPath(pathTemp);
        try {
            db.setDbName(tempDbName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        db.disconnectFromPrivateDB();   // отключиться от старой базы
        db.connectToPrivateDB();           // подключиться к временной базе

        long current = 0;
        for (DBUser record : usersList) {
            // потоки
            String[] decrypted = dispatcher.perform(
                    new DecryptionTask(record.getPassword(), oldKey),
                    record.getOtherData() != null ? new DecryptionTask(record.getOtherData(), oldKey) : null);
            arrayListTask.updateProgressPublic(++current, usersList.size() * 2);

            // потоки
            String[] encrypted = dispatcher.perform(
                    new EncryptionTask(decrypted[0], newKey),
                    decrypted[1] != null ? new EncryptionTask(decrypted[1], newKey) : null);

            // замена старого на новое
            record.setPassword(encrypted[0]);
            record.setOtherData(encrypted[1]);

            arrayListTask.updateProgressPublic(++current, usersList.size() * 2);
        }
    }


    /**
     * Загружает progressBar
     */
    private void showProgress() {
        loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/Progress.fxml"));
        loader.setController(this);

        Pane pane = null;
        try {
            pane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StageComposer.setUpStage(stage, pane, "", 340, 340, 150, 150);
        arrayListTask.updateProgressPublic(0, 1);
        btn_progressFinish.setDisable(true);
        progress_bar.progressProperty().bind(arrayListTask.progressProperty());
        lbl_progressValue.textProperty().bind(Bindings.format("%5.2f%%", arrayListTask.progressProperty().multiply(100)));
    }

    /** Обновляет перешифрованные данные в базе, переносит базу из временной папки, заменяет ключ в объекте KeyWord*/
    private void finish(String newKey){
        db.updateReEncrypted(usersList);  // обновление данных в базе
        db.disconnectFromPrivateDB();      // отключиться от временной базы

        try {
            moveFromTemp(tempDbName, tempDbName);   // перемещение новой базы из временной папки
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyWord.setKey(newKey);
    }


    private void reconnect(String dbName) {
        db.setDbPath(path);
        try {
            db.setDbName(dbName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        db.connectToPrivateDB();
    }

    private void setOKBtn(ActionEvent event) {
        switch (choice) {
            case 'u':
                try {
                    changeUsername();
                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                break;
            case 'p':
                try {
                    changePassword();
                } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                break;
            case 'b':
                try {
                    changeUsernameAndPassword();
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void copyToTemp(String source, String dest) throws IOException {
        File from = new File(path + source + ".db");
        File to = new File(pathTemp + dest + ".db");

        if (tempFolder.exists()) {
            Helper.deleteDirectory(tempFolder);
        }
        if (tempFolder.mkdir()) {
            Files.copy(from.toPath(), to.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    private void moveFromTemp(String source, String dest) throws IOException {
        File from = new File(pathTemp + source + ".db");
        File to = new File(path + dest + ".db");
        Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        Helper.deleteDirectory(tempFolder);
    }



    private void cancel(ActionEvent actionEvent) {
        Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);
        stage.close();
    }


    private class MyTask<V> extends Task {

        @Override
        protected V call() throws Exception {
            return null;
        }

        @Override
        protected void succeeded() {
            btn_progressFinish.setDisable(false);
        }

        public void updateProgressPublic(long workDone, long max) {
            updateProgress(workDone, max);
        }
    }
}