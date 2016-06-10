package passwordProtector.controllers;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import passwordProtector.Main;

import java.io.IOException;
/** Code sample was taken from the Oracle's Ensemble demo*/
public class Popover extends Region {
    private final Region frameBorder = new Region();
    private Pane innerPane;
    private final EventHandler<MouseEvent> popoverHideHandler;
    private Runnable onHideCallback = null;

    private DoubleProperty popoverHeight = new SimpleDoubleProperty(400) {
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    public Popover(AddDataController addDataController, AnchorPane treePane) {
        frameBorder.setMouseTransparent(true);
        frameBorder.getStyleClass().setAll("popover-frame");
        frameBorder.getStylesheets().addAll(Main.class.getResource("css/Popover.bss").toExternalForm());
        frameBorder.setMinWidth(290);
        frameBorder.setMinHeight(264);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/AddDataPop.fxml"));
        loader.setController(addDataController);
        try {
            innerPane = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        innerPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        innerPane.setStyle("-fx-background-color: #f8f9f9");
        innerPane.setLayoutY(10);


        getChildren().addAll(innerPane, frameBorder);
        setVisible(false);
		setOpacity(0);
        setScaleX(.8);
        setScaleY(.8);
        popoverHideHandler = (MouseEvent t) -> {
            // check if event is outside popup and not inside tree
            Point2D mouseInTreePane = treePane.sceneToLocal(t.getX(), t.getY());
            Point2D mouseInFilterPane = sceneToLocal(t.getX(), t.getY());

            boolean outsideTreePane = mouseInTreePane.getX() < 0
                    || mouseInTreePane.getX() > (treePane.getWidth())
                    || mouseInTreePane.getY() < 0
                    || mouseInTreePane.getY() > (treePane.getHeight());

            boolean outsidePopup = mouseInFilterPane.getX() < 0
                    || mouseInFilterPane.getX() > (getWidth())
                    || mouseInFilterPane.getY() < 0
                    || mouseInFilterPane.getY() > (getHeight());

            if (outsideTreePane) {
                if (outsidePopup) {
                    hide();
                    t.consume();
                }
            }
        };
    }

    private Animation fadeAnimation = null;

    public void show(Runnable onHideCallback) {
        if (!isVisible() || fadeAnimation != null) {
            this.onHideCallback = onHideCallback;
            getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, popoverHideHandler);

            if (fadeAnimation != null) {
                fadeAnimation.stop();
                setVisible(true); // for good measure
            } else {
                popoverHeight.set(-1);
                setVisible(true);
            }

            FadeTransition fade = new FadeTransition(Duration.seconds(.1), this);
            fade.setToValue(1.0);
            fade.setOnFinished((ActionEvent event) -> fadeAnimation = null);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(.1), this);
            scale.setToX(1);
            scale.setToY(1);

            ParallelTransition tx = new ParallelTransition(fade, scale);
            fadeAnimation = tx;
            tx.play();
        }
    }

    public void hide() {
        if (isVisible() || fadeAnimation != null) {
            getScene().removeEventFilter(MouseEvent.MOUSE_CLICKED, popoverHideHandler);

            if (fadeAnimation != null) {
                fadeAnimation.stop();
            }

            FadeTransition fade = new FadeTransition(Duration.seconds(.1), this);
            fade.setToValue(0);
            fade.setOnFinished((ActionEvent event) -> {
                fadeAnimation = null;
                setVisible(false);
                if (onHideCallback != null) onHideCallback.run();
            });

            ScaleTransition scale = new ScaleTransition(Duration.seconds(.1), this);
            scale.setToX(.8);
            scale.setToY(.8);

            ParallelTransition tx = new ParallelTransition(fade, scale);
            fadeAnimation = tx;
            tx.play();
        }
    }


}
