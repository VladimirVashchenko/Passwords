package passwordProtector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import passwordProtector.controllers.CustomTextField;
import passwordProtector.controllers.MainController;
import passwordProtector.controllers.SignUpFormController;
import passwordProtector.dataProcessing.DBHelper;
import passwordProtector.dataProcessing.KeyWord;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main extends Application {
    private static Stage primaryStage;
    private final DBHelper db = DBHelper.getInstance();
    private final String path = Helper.filePath();
    private KeyWord keyWord = KeyWord.getInstance();
    private AnchorPane rootPane;
    private TextField username_box;
    private CustomTextField password_box;
    private Label lbl_username, lbl_password;
    private Button btn_ok, btn_cancel;
    private Hyperlink link_signUp;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        if (lockInstance(path + "lock")) {
            launchApp(primaryStage);
        } else if (lockInstance(path + "lock_")) {
            StageComposer.instanceAlertBox(primaryStage);
        } else {
            Platform.exit();
            System.exit(0);
        }
    }

    private void launchApp(Stage primaryStage) throws IOException {
        db.setDbPath(path + "UserData/");
        db.setPublicDbPath(path + "Users/");

        Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);

        // Создаются папки для баз, если их нет
        File files1 = new File(path + "UserData");
        File files2 = new File(path + "Users");

        if (!files1.exists()) {
            files1.mkdirs();
        }
        if (!files2.exists()) {
            files2.mkdirs();
        }

        db.connectToPublicDB();
        db.createTablesPublic();

        loadAuthentication();
    }

    public void loadAuthentication() throws IOException {
        rootPane = new AnchorPane();
        lbl_username = new Label("Логин");
        lbl_password = new Label("Пароль");
        link_signUp = new Hyperlink("Создать пользователя");
        username_box = new TextField();
        password_box = new CustomTextField();
        btn_ok = new Button("Войти");
        btn_cancel = new Button("Закрыть");

        rootPane.getChildren().addAll(lbl_username, lbl_password, username_box, password_box, btn_ok, btn_cancel, link_signUp);

        StageComposer.lblSettings(lbl_username, 25, 49);
        StageComposer.lblSettings(lbl_password, 25, 84);
        StageComposer.textFieldSettings(username_box, "Введите логин", 125, 45, 150);
        StageComposer.textFieldSettings(password_box, "и пароль", 125, 80, 150);

        StageComposer.buttonSettings(btn_ok, 174, 142, 21, 46);
        btn_ok.setDefaultButton(true);
        btn_ok.setMnemonicParsing(false);
        btn_ok.setOnAction(event -> {
            try {
                handleSignInButonAction();
            } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        StageComposer.buttonSettings(btn_cancel, 228, 142, 21, 62);
        btn_cancel.setCancelButton(true);
        btn_cancel.setMnemonicParsing(false);
        btn_cancel.setOnAction(event -> closeApplication());

        link_signUp.setLayoutX(28);
        link_signUp.setLayoutY(142);
        link_signUp.setUnderline(true);
        link_signUp.setOnAction(event -> {
            try {
                handleSignUpLinkAction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        StageComposer.setUpStage(primaryStage, rootPane, "Авторизация", 320, 320, 215, 215);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    private void handleSignInButonAction() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        // validates the user
        if (!username_box.getText().isEmpty() && !password_box.getText().isEmpty()) {
            String username = username_box.getText();
            String password = password_box.getText();
            boolean let_in = db.getUserValidation(username, password);
            if (let_in) {
                String salt = db.getUserSalt(username);
                db.setDbName(username);
                db.connectToPrivateDB();
                db.createTablesPrivate();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Main.class.getResource("view/MainVBox.fxml"));
                AnchorPane rootLayout = loader.load();
                final MainController mainController = loader.<MainController>getController();
                primaryStage.setOnCloseRequest(e -> {
                    e.consume();
                    mainController.shutdownExecutor();
                    closeApplication();
                });

                StageComposer.setUpStage(primaryStage, rootLayout, "Пароли", Double.MAX_VALUE, 700, Double.MAX_VALUE, 500);

                mainController.loadTree();
                mainController.setPrimaryStage(primaryStage);
                mainController.setMain(this);

                String key = keyWord.makeSecretKey(username, password, salt);

                keyWord.setUser(username);
                keyWord.setKey(key);
            } else {
                StageComposer.setNotifier("error", null, "Проверьте данные", Duration.millis(1500), 30, 180, 170, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Заполните все поля", Duration.millis(1500), 30, 180, 170, null);
        }


    }

    @FXML
    private void handleSignUpLinkAction() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        SignUpFormController signUpFormController = new SignUpFormController(this);
        loader.setLocation(Main.class.getResource("view/SignUpDialog.fxml"));
        loader.setController(signUpFormController);

        AnchorPane rootLayout = loader.load();

        StageComposer.setUpStage(primaryStage, rootLayout, "Создание пользователя", 320, 320, 250, 250);
    }

    private boolean lockInstance(final String lockFile) {
        try {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // отключиться от баз и закрыть программу
    @FXML
    public void closeApplication() {
        db.disconnectFromPrivateDB();
        db.disconnectFromPublicDB();
        Platform.exit();
        System.exit(0);
    }
}
