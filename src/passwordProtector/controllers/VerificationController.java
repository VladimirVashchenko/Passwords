package passwordProtector.controllers;

import javafx.animation.*;
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
import javafx.scene.shape.SVGPath;
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

    @FXML
    SVGPath svg_background;
    @FXML
    Button btn_deleteAccount;
    private final Timeline timeline = new Timeline();


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
                StageComposer.setNotifier("error", null, "Проверьте данные", Duration.millis(1500), 30, 180, stage.getMaxHeight() - 60, null);
            }
        } else {
            StageComposer.setNotifier("warning", null, "Заполните все поля", Duration.millis(1500), 30, 180, stage.getMaxHeight() - 60, null);
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
        StageComposer.setUpStage(stage, rootLayout, "", 420, 420, 200, 200);
        Interpolator rectInterpolator = new Interpolator() {
            private static final double S1 = -25.0 / 9.0;
            private static final double S2 = 50.0 / 9.0;
            private static final double S3 = -16.0 / 9.0;
            private static final double S4 = 10.0 / 9.0;

            @Override
            protected double curve(double t) {
                // See the SMIL 3.1 specification for details on this calculation
                // acceleration = 0.2, deceleration = 0.2
                //return clamp((t < 0.1) ? 3.125 * t * t : (t > 0.7) ? -3.125 * t * t + 6.25 * t - 2.125 /*sin(x+0.6)*3-2.164*/ : 1.25 * t - 0.125);
                return clamp((t > 0.8) ? S1 * t * t + S2 * t + S3 : S4 * t);
            }

            private double clamp(double t) {
                return (t < 0.0) ? 0.0 : (t > 1.0) ? 1.0 : t;
            }

        };


        KeyValue rectKeyValue = new KeyValue(svg_background.rotateProperty(), -720, /*rectInterpolator*/Interpolator.LINEAR);
        //create a keyFrame with duration 4s
        KeyFrame rectKeyFrame = new KeyFrame(Duration.seconds(0.6), rectKeyValue);
        //add the keyframe to the timeline
        timeline.getKeyFrames().add(rectKeyFrame);
        Timeline timeline1 = new Timeline();

        KeyValue rectKeyValue1 = new KeyValue(svg_background.rotateProperty(), -720, Interpolator.EASE_OUT);
        //create a keyFrame with duration 4s
        KeyFrame rectKeyFrame1 = new KeyFrame(Duration.seconds(0.6), rectKeyValue1);
        //add the keyframe to the timeline

        KeyFrame moveBall = new KeyFrame(Duration.seconds(0.6),
                event -> {
                    //timeline.getKeyFrames().add(rectKeyFrame);
                    //timeline.pause();
                    //timeline.playFrom(Duration.seconds(0));
                    svg_background.setRotate(0);
                    //timeline1.play();
                });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                svg_background.rotateProperty().set(now++);

                stop();
            }
        };

        timeline1.getKeyFrames().add(rectKeyFrame1);
        timeline.getKeyFrames().add(moveBall);
        btn_deleteAccount.setOnAction(event -> timeline.play()/*timer.start()*/);
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
            StageComposer.setNotifier("warning", null, "Сделайте выбор!", Duration.millis(2000), 30, 230, stage.getMaxHeight() - 60, null);
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
