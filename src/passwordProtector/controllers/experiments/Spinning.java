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
    SVGPath svg_background, svg_smallMan, svg_hand;
    @FXML
    Button btn_deleteAccount;
    @FXML
    Group group_smallMan;

    private final Timeline timeline = new Timeline();
    private final Timeline timeline1 = new Timeline();


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
        StageComposer.setUpStage(primaryStage, rootLayout, "", 420, 420, 500, 500);
        Interpolator rectInterpolator = new Interpolator() {
            @Override
            protected double curve(double t) {
                //return clamp((t < 0.1) ? 3.125 * t * t : (t > 0.5) ? -3.125 * t * t + 6.25 * t - 2.125 : 1.25 * t - 0.125);
                return t * 0.981;
            }

            private double clamp(double t) {
                return (t < 0.0) ? 0.0 : (t > 1.0) ? 1.0 : t;
            }

        };

        KeyValue vortexKeyValue = new KeyValue(svg_background.rotateProperty(), svg_background.rotateProperty().doubleValue() - 360, Interpolator.SPLINE(0.3, 1, 0.8, 1));
        KeyFrame vortexKeyFrame = new KeyFrame(Duration.seconds(2), vortexKeyValue);

        KeyValue vortexVisibilityKeyValue = new KeyValue(svg_background.opacityProperty(), 1, Interpolator.SPLINE(0.3, 1, 0.8, 1));
        KeyFrame vortexVisibilityKeyFrame = new KeyFrame(Duration.seconds(0.45), vortexVisibilityKeyValue);

        KeyFrame rotate = new KeyFrame(Duration.seconds(0.45),
                event -> {
                    //svg_background.setRotate(0);
                    timeline1.play();

                });

        //small man
        KeyValue smallManTXKeyValue = new KeyValue(group_smallMan.translateXProperty(), group_smallMan.getTranslateX() - 6, Interpolator.LINEAR);
        KeyFrame smallManTXKeyFrame = new KeyFrame(Duration.seconds(0.4), smallManTXKeyValue);

        KeyValue smallManTYKeyValue = new KeyValue(svg_smallMan.translateYProperty(), 59, Interpolator.SPLINE(1, 1, 0.5, 1));
        KeyFrame smallManTYKeyFrame = new KeyFrame(Duration.seconds(0.4), smallManTYKeyValue);

        KeyValue smallManRotKeyValue = new KeyValue(svg_smallMan.rotateProperty(), svg_smallMan.getRotate() - 430, Interpolator.SPLINE(0.7, 0.4, 0.8, 1));
        KeyFrame smallManRotKeyFrame = new KeyFrame(Duration.seconds(0.4), smallManRotKeyValue);

        KeyValue handRotKeyValue = new KeyValue(svg_hand.rotateProperty(), -184, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handRotKeyFrame = new KeyFrame(Duration.seconds(0.2), handRotKeyValue);

        KeyValue handTYKeyValue = new KeyValue(svg_hand.translateYProperty(), 16, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handTYKeyFrame = new KeyFrame(Duration.seconds(0.18), handTYKeyValue);

        KeyValue handTXKeyValue = new KeyValue(svg_hand.translateXProperty(), -0.3, Interpolator.SPLINE(0.8, 1, 1, 1));
        KeyFrame handTXKeyFrame = new KeyFrame(Duration.seconds(0.2), handTXKeyValue);

        timeline1.getKeyFrames().addAll(vortexVisibilityKeyFrame, vortexKeyFrame);
        timeline.getKeyFrames().addAll(/*vortexKeyFrame,*/ rotate,
                smallManTXKeyFrame, smallManTYKeyFrame, smallManRotKeyFrame,
                handRotKeyFrame,
                handTXKeyFrame, handTYKeyFrame);
        btn_deleteAccount.setOnAction(event -> timeline.play()/*timer.start()*/);
    }
}
