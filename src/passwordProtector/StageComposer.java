package passwordProtector;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by Administrator on 21.04.2016.
 */
public class StageComposer {
    private static Notification.Notifier notifier;

    public static Stage setUpStage(Stage stage, Pane rootLayout, String title,
                                   double maxWidth, double minWidth, double maxHeight, double minHeight) {
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add(Main.class.getResource("css/caspian.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("css/notifier.css").toExternalForm());

        stage.setTitle(title);
        stage.setMaxHeight(maxHeight);
        stage.setMaxWidth(maxWidth);
        stage.setMinHeight(minHeight);
        stage.setMinWidth(minWidth);

        stage.hide();
        stage.setScene(scene);
        stage.show();
        rootLayout.requestFocus();
        return stage;
    }

    public static void setNotifier(String notificationType, String title, String message, Duration POPUP_LIFETIME, double HEIGHT, double WIDTH, double OFFSET_Y, SVGPath svgPath) {
        notifier = Notification.Notifier.INSTANCE;
        notifier.setPopupLifetime(POPUP_LIFETIME);
        notifier.setHeight(HEIGHT);
        notifier.setWidth(WIDTH);
        notifier.setOffsetY(OFFSET_Y);

        switch (notificationType) {
            case "notify":
                notifier.notify(title, message, svgPath);
                break;
            case "info":
                notifier.notifyInfoPath(title, message);
                break;
            case "warning":
                notifier.notifyWarningPath(title, message);
                break;
            case "success":
                notifier.notifySuccessPath(title, message);
                break;
            case "error":
                notifier.notifyErrorPath(title, message);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void instanceAlertBox(Stage primaryStage) {
        Button button = new Button("Закрыть");
        TextFlow txtf = new TextFlow();
        Text message = new Text("Программа уже запущена!");
        AnchorPane layout = new AnchorPane();
        txtf.getChildren().add(message);
        layout.getChildren().addAll(txtf, button);

        Stage stage = setUpStage(primaryStage, layout, "Внимание!", 280, 280, 150, 150);
        stage.setResizable(false);

        Double primaryStageHeight = stage.getHeight();
        Double primaryStageWidth = stage.getWidth();

        Double txtfWidth = txtf.getWidth();

        Double buttonHeigth = button.getHeight();
        Double buttonWidth = button.getWidth();

        AnchorPane.setLeftAnchor(button, primaryStageWidth * 0.5 - buttonWidth * 0.5);
        AnchorPane.setBottomAnchor(button, primaryStageHeight * 0.25 - buttonHeigth * 0.5);
        AnchorPane.setTopAnchor(txtf, 25.0);
        AnchorPane.setLeftAnchor(txtf, primaryStageWidth * 0.5 - txtfWidth * 0.5);


        button.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
