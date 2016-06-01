package passwordProtector.controllers.experiments;

import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import passwordProtector.Main;
import passwordProtector.StageComposer;

import java.io.IOException;

/**
 * Created by Administrator on 31.05.2016.
 */
public class Spinning extends Application {
    @FXML
    SVGPath svg_bigVortex, svg_smallVortex, svg_smallMan, svg_hand;
    @FXML
    Button btn_deleteAccount;
    @FXML
    Group group_smallMan;

    private final Timeline timeline = new Timeline();
    private final Timeline timeline1 = new Timeline();
    private final Timeline timeline2 = new Timeline();


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/OptionsDialog.fxml"));
        loader.setController(this);

        AnchorPane rootLayout = null;
        try {
            rootLayout = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StageComposer.setUpStage(primaryStage, rootLayout, "", 420, 420, 200, 200);

        KeyValue bigVKV_tl2 = new KeyValue(svg_bigVortex.rotateProperty(), svg_bigVortex.rotateProperty().doubleValue() - 360, Interpolator.SPLINE(0.4, 1, 0.9, 1));
        KeyFrame bigVKF_tl2 = new KeyFrame(Duration.seconds(1.5), bigVKV_tl2);

        KeyValue smallVKV_tl2 = new KeyValue(svg_smallVortex.rotateProperty(), svg_smallVortex.rotateProperty().doubleValue() - 360, Interpolator.SPLINE(1, 1, 0.9, 0.6));
        KeyFrame smallVKF_tl2 = new KeyFrame(Duration.seconds(2), smallVKV_tl2);

        KeyValue bigVOpacityPlusKV_tl2 = new KeyValue(svg_bigVortex.opacityProperty(), 1, Interpolator.SPLINE(1, 1, 0.8, 1));
        KeyValue smallVOpacityPlusKV_tl2 = new KeyValue(svg_smallVortex.opacityProperty(), 1, Interpolator.SPLINE(1, 1, 0.8, 1));
        KeyFrame vortexOpacityPlusKF_tl2 = new KeyFrame(Duration.seconds(0.14), bigVOpacityPlusKV_tl2, smallVOpacityPlusKV_tl2); //****

        KeyValue bigVOpacityMinusKV = new KeyValue(svg_bigVortex.opacityProperty(), 0, Interpolator.SPLINE(0.3, 0.8, 0.5, 1));
        KeyValue bigVScaleXMinusKV = new KeyValue(svg_bigVortex.scaleXProperty(), 0, Interpolator.SPLINE(1, 1, 1, 1));
        KeyValue bigVScaleYMinusKV = new KeyValue(svg_bigVortex.scaleYProperty(), 0, Interpolator.SPLINE(0.8, 1, 1, 1));

        KeyValue smallVOpacityMinusKV = new KeyValue(svg_smallVortex.opacityProperty(), 0, Interpolator.SPLINE(0.3, 0.8, 0.5, 1));
        KeyValue smallVScaleXMinusKV = new KeyValue(svg_smallVortex.scaleXProperty(), 0, Interpolator.SPLINE(1, 1, 1, 1));
        KeyValue smallVScaleYMinusKV = new KeyValue(svg_smallVortex.scaleYProperty(), 0, Interpolator.SPLINE(0.8, 1, 1, 1));

        KeyValue smallManOpacityMinusKV = new KeyValue(svg_smallMan.opacityProperty(), 0, Interpolator.SPLINE(1, 1, 0.9, 1));
        KeyFrame vortexOpacityMinusKF = new KeyFrame(Duration.seconds(0.60), smallManOpacityMinusKV, //****
                bigVOpacityMinusKV, bigVScaleXMinusKV, bigVScaleYMinusKV,
                smallVOpacityMinusKV, smallVScaleXMinusKV, smallVScaleYMinusKV);

        KeyFrame vortexAppears = new KeyFrame(Duration.seconds(0.45), // время, через которое появляется воронка
                event -> {
                    svg_bigVortex.setRotate(0);
                    svg_bigVortex.setScaleX(1);
                    svg_bigVortex.setScaleY(1);
                    svg_smallVortex.setRotate(0);
                    svg_smallVortex.setScaleX(0.7);
                    svg_smallVortex.setScaleY(0.8);
                    timeline1.play();

                });

        KeyFrame vortexDisappears = new KeyFrame(Duration.seconds(1), // время, через которое воронка начинает исчезать
                event -> {
                    timeline2.play();
                });

        //small man
        KeyValue smallManTXKV_tl1 = new KeyValue(group_smallMan.translateXProperty(), group_smallMan.getTranslateX() - 6, Interpolator.LINEAR);
        KeyValue smallManTYKV_tl1 = new KeyValue(svg_smallMan.translateYProperty(), 59, Interpolator.SPLINE(1, 1, 0.5, 1));
        KeyValue smallManRotKV_tl1 = new KeyValue(svg_smallMan.rotateProperty(), svg_smallMan.getRotate() - 430, Interpolator.SPLINE(0.7, 0.4, 0.8, 1));
        KeyFrame smallManRotKF_tl1 = new KeyFrame(Duration.seconds(0.4), smallManRotKV_tl1, smallManTXKV_tl1, smallManTYKV_tl1);


        KeyValue handRotKV_tl1 = new KeyValue(svg_hand.rotateProperty(), -184, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyValue handTXKV_tl1 = new KeyValue(svg_hand.translateXProperty(), -0.3, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handKF_tl1 = new KeyFrame(Duration.seconds(0.2), handTXKV_tl1, handRotKV_tl1);

        KeyValue handTYKV_tl1 = new KeyValue(svg_hand.translateYProperty(), 16, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handTYKF_tl1 = new KeyFrame(Duration.seconds(0.18), handTYKV_tl1);


        timeline.getKeyFrames().addAll(vortexAppears,
                smallManRotKF_tl1,
                handKF_tl1, handTYKF_tl1);
        timeline1.getKeyFrames().addAll(vortexOpacityPlusKF_tl2, bigVKF_tl2, smallVKF_tl2, vortexDisappears);
        timeline2.getKeyFrames().add(vortexOpacityMinusKF);

        btn_deleteAccount.setOnAction(event -> timeline.play());
    }
}
