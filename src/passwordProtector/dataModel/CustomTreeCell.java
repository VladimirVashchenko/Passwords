package passwordProtector.dataModel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import passwordProtector.controllers.MainController;

/**
 * Created by Administrator on 23.01.2016.
 */
public class CustomTreeCell extends TreeCell<Branch> {
    private MainController mainController;
    private TextField textField;
    private final ContextMenu contextMenuBranch = new ContextMenu();

    public CustomTreeCell(MainController mainController) {
        this.mainController = mainController;

        MenuItem menuEdit = new MenuItem("Изменить");
        MenuItem menuDelete = new MenuItem("Удалить");

        contextMenuBranch.getItems().addAll(menuEdit, new SeparatorMenuItem(), new SeparatorMenuItem(), menuDelete);

        menuEdit.setOnAction((ActionEvent t) -> edit());
        menuDelete.setOnAction((ActionEvent t) -> mainController.handleDeleteBtn());
    }


    private void edit() {
        super.startEdit();
        if (textField == null) {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();

    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().toString());
        setGraphic(getTreeItem().getGraphic());
    }

    @Override
    public void updateItem(Branch item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(getTreeItem().getGraphic());
                setContextMenu(contextMenuBranch);
            }
        }
    }

    private void createTextField() {
        if (!getTreeItem().getParent().getValue().getName().equals("Root")) {
            mainController.setTempUsername(getString());
        }
        textField = new TextField(getString());
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!textField.getText().isEmpty() && !textField.getText().matches(".*\\s+.*")) {
                    if (getTreeItem().getParent().getValue().getName().equals("Root")) {
                        mainController.saveLocation(textField.getText());
                    } else {
                        mainController.saveUsername(textField.getText());
                    }
                } else {
                    cancelEdit();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }


    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }


    @Override
    public String toString() {
        return "CustomTreeCell";
    }
}
