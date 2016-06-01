package passwordProtector.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import passwordProtector.*;
import passwordProtector.dataProcessing.DBHelper;
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
    private SVGPath svg_bigVortex, svg_smallVortex, svg_smallMan, svg_hand;
    @FXML
    private Button btn_deleteAccount;
    @FXML
    private Group group_smallMan;

    private final Timeline timeline = new Timeline();
    private final Timeline timeline1 = new Timeline();
    private final Timeline timeline2 = new Timeline();


    private String oldUsername, oldPassword;
    private Main main;
    private final DBHelper db = DBHelper.getInstance();
    private final KeyWord keyWord = KeyWord.getInstance();

    VerificationController(Stage stage, Stage primaryStage) {
        this.stage = stage;
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_verifyOk.setOnAction(event -> verify(edit_verifyLogin.getText(), edit_verifyPass.getText()));
        btn_verifyCancel.setOnAction(this::cancel);
    }

    public void setMain(Main main) {
        this.main = main;
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

        deletionAnimation();

        btn_deleteAccount.setOnAction(event -> timeline.play());
        btn_optionsNext.setOnAction(this::options);
        btn_optionsCancel.setOnAction(this::cancel);
    }

    private void deletionAnimation() {
        // ворорнки
        KeyValue bigVKV_tl2 = new KeyValue(svg_bigVortex.rotateProperty(), svg_bigVortex.rotateProperty().doubleValue() - 360, Interpolator.SPLINE(0.4, 1, 0.9, 1));
        KeyFrame bigVKF_tl2 = new KeyFrame(Duration.seconds(1.5), bigVKV_tl2);

        KeyValue smallVKV_tl2 = new KeyValue(svg_smallVortex.rotateProperty(), svg_smallVortex.rotateProperty().doubleValue() - 360, Interpolator.SPLINE(1, 1, 0.9, 0.6));
        KeyFrame smallVKF_tl2 = new KeyFrame(Duration.seconds(2), smallVKV_tl2);

        KeyValue bigVOpacityPlusKV_tl2 = new KeyValue(svg_bigVortex.opacityProperty(), 1, Interpolator.SPLINE(1, 1, 0.8, 1));
        KeyValue smallVOpacityPlusKV_tl2 = new KeyValue(svg_smallVortex.opacityProperty(), 1, Interpolator.SPLINE(1, 1, 0.8, 1));
        KeyFrame vortexOpacityPlusKF_tl2 = new KeyFrame(Duration.seconds(0.44), bigVOpacityPlusKV_tl2, smallVOpacityPlusKV_tl2); //****

        KeyValue bigVOpacityMinusKV = new KeyValue(svg_bigVortex.opacityProperty(), 0, Interpolator.SPLINE(0.3, 0.8, 0.5, 1));
        KeyValue bigVScaleXMinusKV = new KeyValue(svg_bigVortex.scaleXProperty(), 0, Interpolator.SPLINE(1, 1, 1, 1));
        KeyValue bigVScaleYMinusKV = new KeyValue(svg_bigVortex.scaleYProperty(), 0, Interpolator.SPLINE(0.8, 1, 1, 1));

        KeyValue smallVOpacityMinusKV = new KeyValue(svg_smallVortex.opacityProperty(), 0, Interpolator.SPLINE(0.3, 0.8, 0.5, 1));
        KeyValue smallVScaleXMinusKV = new KeyValue(svg_smallVortex.scaleXProperty(), 0, Interpolator.SPLINE(1, 1, 1, 1));
        KeyValue smallVScaleYMinusKV = new KeyValue(svg_smallVortex.scaleYProperty(), 0, Interpolator.SPLINE(0.8, 1, 1, 1));

        KeyValue smallManOpacityMinusKV = new KeyValue(svg_smallMan.opacityProperty(), 0, Interpolator.SPLINE(1, 1, 0.9, 1));
        KeyFrame vortexOpacityMinusKF = new KeyFrame(Duration.seconds(0.60), smallManOpacityMinusKV,                                        //****
                bigVOpacityMinusKV, bigVScaleXMinusKV, bigVScaleYMinusKV,
                smallVOpacityMinusKV, smallVScaleXMinusKV, smallVScaleYMinusKV);

        KeyFrame resetVortices = new KeyFrame(Duration.seconds(0.65), // вызвать диалог для подтверждения
                event -> {

                    Platform.runLater(() -> deleteAccount());

                });

        KeyFrame vortexDisappears = new KeyFrame(Duration.seconds(1), // анимация воронки
                event -> {
                    timeline2.play();
                });

        KeyFrame callDeletion = new KeyFrame(Duration.seconds(0.8), event -> { // после анимации воронки - удалить аккаунт
            db.disconnectFromPrivateDB();
            Helper.deleteFile(Helper.filePath()+"UserData/",keyWord.getUser());
            db.deletePublicAccount(keyWord.getUser());
            Notification.Notifier.setPopupLocation(primaryStage, Pos.BOTTOM_CENTER);
            stage.close();
            primaryStage.hide();

            try {
                main.loadAuthentication();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // человечек
        KeyValue smallManTXKV_tl1 = new KeyValue(group_smallMan.translateXProperty(), group_smallMan.getTranslateX() - 6, Interpolator.LINEAR);
        KeyValue smallManTYKV_tl1 = new KeyValue(svg_smallMan.translateYProperty(), 59, Interpolator.SPLINE(1, 1, 0.5, 1));
        KeyValue smallManRotKV_tl1 = new KeyValue(svg_smallMan.rotateProperty(), svg_smallMan.getRotate() - 430, Interpolator.SPLINE(0.7, 0.4, 0.8, 1));
        KeyFrame smallManRotKF_tl1 = new KeyFrame(Duration.seconds(0.4), smallManRotKV_tl1, smallManTXKV_tl1, smallManTYKV_tl1);

        // рука
        KeyValue handRotKV_tl1 = new KeyValue(svg_hand.rotateProperty(), -184, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyValue handTXKV_tl1 = new KeyValue(svg_hand.translateXProperty(), -0.3, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handKF_tl1 = new KeyFrame(Duration.seconds(0.2), handTXKV_tl1, handRotKV_tl1);

        KeyValue handTYKV_tl1 = new KeyValue(svg_hand.translateYProperty(), 16, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handTYKF_tl1 = new KeyFrame(Duration.seconds(0.18), handTYKV_tl1);


        timeline.getKeyFrames().addAll(resetVortices,
                smallManRotKF_tl1,
                handKF_tl1, handTYKF_tl1);
        timeline1.getKeyFrames().addAll(vortexOpacityPlusKF_tl2, bigVKF_tl2, smallVKF_tl2, vortexDisappears);
        timeline2.getKeyFrames().addAll(vortexOpacityMinusKF, callDeletion);
    }

    private void deleteAccount() {
        if (AlertDialog.alert(primaryStage, "Удаление аккаунта", "Внимание! Ваш аккаунт и все связанные с ним данные будут удалены. Вы подтверждаете удаление?", "warning")) {
            svg_bigVortex.setRotate(0);
            svg_bigVortex.setScaleX(1);
            svg_bigVortex.setScaleY(1);
            svg_smallVortex.setRotate(0);
            svg_smallVortex.setScaleX(0.7);
            svg_smallVortex.setScaleY(0.8);
            timeline1.play();
        } else {
            svg_hand.setRotate(-104);
            svg_hand.setTranslateX(0);
            svg_hand.setTranslateY(0);

            svg_smallMan.setTranslateX(0);
            svg_smallMan.setTranslateY(0);
            svg_smallMan.setRotate(-123.7);
        }
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
