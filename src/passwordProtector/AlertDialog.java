package passwordProtector;


import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class AlertDialog {
    public static boolean answer;

    public static boolean alert(Stage owner, String title, String message, String icon) {
        Stage stage = new Stage();
        VBox layout = new VBox();
        HBox top = new HBox();
        HBox bottom = new HBox();
        Scene scene = new Scene(layout);
        stage.initOwner(owner);

        Image image = new Image(AlertDialog.class.getResourceAsStream("img/" + icon + ".png"));
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        TextFlow txtFlow = new TextFlow();
        Text text = new Text(message);
        txtFlow.getChildren().add(text);

        Button yesBtn = new Button("Да");
        Button noBtn = new Button("Нет");
        Region region = new Region();

        layout.setPrefWidth(330);
        top.setMinHeight(60);
        bottom.setPrefHeight(28);
        bottom.setPrefWidth(330);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        yesBtn.setMinWidth(60);
        noBtn.setMinWidth(60);

        VBox.setMargin(top, new Insets(0, 10, 0, 0));
        VBox.setMargin(bottom, new Insets(0, 0, 10, 0));
        HBox.setMargin(imageView, new Insets(30, 0, 0, 20));
        HBox.setMargin(txtFlow, new Insets(20, 20, 10, 20));
        HBox.setMargin(yesBtn, new Insets(10, 0, 0, 40));
        HBox.setMargin(noBtn, new Insets(10, 40, 0, 0));
        HBox.setHgrow(region, Priority.ALWAYS);

        top.getChildren().addAll(imageView, txtFlow);
        bottom.getChildren().addAll(yesBtn, region, noBtn);
        layout.getChildren().addAll(top, bottom);

        noBtn.setOnAction(event -> {
            answer = false;
            stage.close();
        });
        yesBtn.setOnAction(event -> {
            answer = true;
            stage.close();
        });

        scene.getStylesheets().add(Main.class.getResource("css/caspian.css").toExternalForm());
        stage.setMinWidth(330);
        stage.setMinHeight(165);
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();

        return answer;
    }

}
