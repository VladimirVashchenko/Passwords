package passwordProtector.controllers;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import passwordProtector.AlertDialog;
import passwordProtector.Main;
import passwordProtector.StageComposer;
import passwordProtector.dataModel.Branch;
import passwordProtector.dataModel.CustomTreeCell;
import passwordProtector.dataModel.TreeViewWithItems;
import passwordProtector.dataModel.User;
import passwordProtector.dataProcessing.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    @FXML
    private AnchorPane treePane, rightPane, pane_main, footer, middlePane;
    @FXML
    private Button btn_usernameCopy, btn_passwordCopy, btn_saveOtherData;
    @FXML
    private ToggleButton tgl_test;
    @FXML
    private TextField edit_usernameMain, edit_findLocation;
    @FXML
    private TextArea txt_otherData;
    @FXML
    private CheckBox check_setEdit;
    @FXML
    private Label lbl_login, lbl_pass, lbl_other, lbl_data;
    private Tooltip tt_copy, tt_search, tt_context, tt_save;

    private final DBHelper db;
    private final KeyWord keyWord;
    private Main main;
    private AddDataController addDataController;
    private Stage primaryStage;
    private Popover popover;
    private CustomTextField edit_passwordMain;
    private TreeViewWithItems<Branch> treeView;
    private final TreeItem<Branch> rootNode = new TreeItem<>(new Branch("Root"));
    private final EventHandler<MouseEvent> selectionHandler;

    private String tempPassword = "";
    private String tempOtherData = "";
    private String tempUsername = "";

    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ClipboardContent content = new ClipboardContent();

    private final Dispatcher dispatcher = Dispatcher.getInstance();
    private final EventExecutorService eventExService = dispatcher.getEventExService();

    private final ListChangeListener<User> listener = getListOfUsersListener();
    private final ChangeListener<? super TreeItem<Branch>> treelistener = getTreelistener();

    private final Callback<User, Observable[]> callback = (User user) -> new Observable[]{user.usernameProperty(), user.locationProperty()};
    private final ObservableList<User> listOfUsers = FXCollections.observableArrayList(callback);
    private final ObservableList<Branch> TREE_VIEW_BRANCHES = FXCollections.observableArrayList();
    private final ConcurrentMap<String, Branch> MAP_BRANCHES = new ConcurrentHashMap<>();    // ссылки на все Branch - сервисы - (используется для просмотра children конкретного Branch)
    private final ConcurrentMap<String, User> MAP_LOCATIONS = new ConcurrentHashMap<>(); // ссылки на объекты User с пустыми username
    private final ConcurrentMap<String, User> MAP_USERS = new ConcurrentHashMap<>();

    public MainController() {
        db = DBHelper.getInstance();
        keyWord = KeyWord.getInstance();
        selectionHandler = (MouseEvent t) -> {
            setLocation();
            t.consume();
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Collection<Node> keepers = middlePane.getChildren().stream().collect(Collectors.toCollection(ArrayList::new));
        middlePane.getChildren().retainAll(keepers);

        addDataController = new AddDataController(this);
        popover = new Popover(addDataController, treePane);
        middlePane.getChildren().add(popover);

        Collection<Node> keepers1 = rightPane.getChildren().stream().collect(Collectors.toCollection(ArrayList::new));
        rightPane.getChildren().retainAll(keepers1);

        edit_passwordMain = new CustomTextField();
        edit_passwordMain.setPrefWidth(edit_usernameMain.getPrefWidth());
        edit_passwordMain.setLayoutX(edit_usernameMain.getLayoutX());
        edit_passwordMain.setLayoutY(67);
        edit_passwordMain.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        rightPane.getChildren().add(edit_passwordMain);

        edit_findLocation.textProperty().addListener(observable -> {
            selectTreeItem(edit_findLocation.textProperty().getValue());
        });

        edit_findLocation.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                edit_findLocation.clear();
            }
        });

        tgl_test.setOnMouseClicked((MouseEvent e) -> {
            if (popover.isVisible()) {
                popover.hide();
            } else {
                popover.show(() -> tgl_test.setSelected(false));
                AnchorPane.setRightAnchor(popover, AnchorPane.getRightAnchor(tgl_test));
                AnchorPane.setBottomAnchor(popover, AnchorPane.getBottomAnchor(tgl_test));
                StageComposer.setNotifier("info", null, "Для автоматической подстановки, \nвыделите сервис или пользователя.", Duration.millis(4500), 60, 260, 15, null);
            }
        });

        btn_saveOtherData.setOnAction(event -> saveOtherData());

        // set checkbox listener
        check_setEdit.selectedProperty().addListener((observable, oldValue, newValue) -> CheckBoxListener(newValue));

        // bind to checkbox
        btn_saveOtherData.visibleProperty().bind(check_setEdit.selectedProperty());
        txt_otherData.editableProperty().bind(check_setEdit.selectedProperty());
        edit_passwordMain.editableProperty().bind(check_setEdit.selectedProperty());

        setVisibility(false);

        /*
         * Tooltips
         */
        tt_context = makeToolTip("Правый клик - для вызова\nконтекстного меню.");
        tt_copy = makeToolTip("Копировать в буфер обмена");
        tt_save = makeToolTip("Сохранить изменения в базе данных");
        tt_search = makeToolTip("Начните ввод.");

        btn_usernameCopy.setTooltip(tt_copy);
        btn_passwordCopy.setTooltip(tt_copy);
        btn_saveOtherData.setTooltip(tt_save);
        edit_findLocation.setTooltip(tt_search);
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setTempUsername(String username) {
        tempUsername = username;
    }

    /**
     * Генерация дерева из записей в базе данных с расшифровыванием полученных
     * паролей, отображение логина и пароля выбранного в дереве пользователя в
     * полях ввода/вывода логина и пароля
     */
//=========================================================================
    public void loadTree() {
        listOfUsers.addListener(listener);
        makeTree();
        treeView.requestFocus();

        ObservableList<User> dataSet = db.getUserDataAsList();
        if (dataSet.size() > 0) {
            makeNewList(dataSet, 0);
        }
        treeView.setTooltip(tt_context);
    }

    private void makeTree() {
        treeView = new TreeViewWithItems<>();
        treeView.setEditable(true);
        treeView.setCellFactory(p -> new CustomTreeCell(this));
        treeView.setRoot(rootNode);
        treeView.setShowRoot(false);
        treeView.setItems(TREE_VIEW_BRANCHES);

        treePane.getChildren().add(treeView);
        AnchorPane.setLeftAnchor(treeView, 0d);
        AnchorPane.setRightAnchor(treeView, 0d);
        AnchorPane.setBottomAnchor(treeView, 0d);
        AnchorPane.setTopAnchor(treeView, 0d);

        treeView.getSelectionModel().selectedItemProperty().addListener(treelistener);
        treeView.addEventFilter(MouseEvent.MOUSE_CLICKED, selectionHandler);
    }

    /**
     * Item selection listener
     */

    private ChangeListener<? super TreeItem<Branch>> getTreelistener() {
        return (observableValue, oldItem, newItem) -> {
            if ((newItem != null) && (newItem != rootNode) && newItem.isLeaf() && (newItem.getParent() != rootNode)) {
                Runnable task = () -> {
                    //long timeMillis = System.currentTimeMillis();
                    String[] userdata = db.getUserData(newItem.getParent().getValue().getName(), newItem.getValue().getName());

                    // расшифровка в два потока
                    String[] processed = dispatcher.perform(
                            new DecryptionTask(userdata[1], keyWord.getKey()),
                            userdata[2] != null ? new DecryptionTask(userdata[2], keyWord.getKey()) : null);

                    String username = userdata[0];
                    String password = processed[0];
                    String otherData = null;
                    try {
                        otherData = processed[1] != null ? new String(processed[1].getBytes("UTF-8"), "UTF-8") : "";
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    edit_usernameMain.setText(username);
                    edit_passwordMain.setText(password);
                    txt_otherData.setText(otherData);

                    tempPassword = password;
                    tempOtherData = otherData;

                    //long after = System.currentTimeMillis();
                    //System.out.println("Time to extract and decrypt: " + (after - timeMillis));
                };

                setVisibility(true);
                eventExService.execute(task);


            } else {
                setVisibility(false);
                check_setEdit.setSelected(false);
                txt_otherData.setText("");
                edit_usernameMain.setText("");
                edit_passwordMain.setText("");
            }
        };
    }

    private ListChangeListener<User> getListOfUsersListener() {
        return change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    // It looks horrible, but it works. This will force the tree to update the tree labels.
                    TreeItem<Branch> currentTreeItem = treeView.getSelectionModel().getSelectedItem();
                    int index = currentTreeItem.getParent().getChildren().indexOf(currentTreeItem);
                    currentTreeItem.getParent().getChildren().set(index, currentTreeItem);

                }
                if (change.wasRemoved()) {

                    for (User user : change.getRemoved()) {
                        if (user.getUsername() != null) {
                            //user
                            MAP_USERS.remove(user.toString());
                            MAP_BRANCHES.get(user.getLocation()).removeChild(user.getUsername()); // удаление Branch для пользователя из списка children в Branch-е для сервиса
                            deleteUser(user.getLocation(), user.getUsername());
                        } else {
                            //location
                            TREE_VIEW_BRANCHES.remove(MAP_BRANCHES.get(user.getLocation()));
                            MAP_BRANCHES.remove(user.getLocation());
                            MAP_LOCATIONS.remove(user.getLocation());
                            deleteLocation(user.getLocation());
                        }
                    }

                }
                // If items have been added
                if (change.wasAdded()) {
                    // Get the new items
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        User user = change.getList().get(i);
                        String username = user.getUsername();
                        String location = user.getLocation();

                        if (username == null) {
                            Branch branch = newBranch(location);
                            branch.nameProperty().bind(user.locationProperty()); // связать свойство nameProperty созданного Branch сервиса со свойством locationProperty объекта User сервиса, из которого он создан
                            MAP_LOCATIONS.put(location, user);                   // сохранить ссылку на объект User сервиса в HashMap
                            MAP_BRANCHES.put(location, branch);                  // сохранить ссылку на объект Branch сервиса в HashMap
                            TREE_VIEW_BRANCHES.add(branch);                      // добавить созданный объект Branch сервиса в список дерева
                            FXCollections.sort(TREE_VIEW_BRANCHES);
                        } else {
                            Branch userBranch = newBranch(username);
                            ObservableList<Branch> children = MAP_BRANCHES.get(location).getChildren();

                            user.locationProperty().bindBidirectional(MAP_LOCATIONS.get(user.getLocation()).locationProperty()); // связать свойство locationProperty объекта User пользователя со свойством locationProperty объекта User сервиса
                            userBranch.nameProperty().bindBidirectional(user.usernameProperty());                                // связать свойство nameProperty созданного Branch пользователя со свойством usernameProperty объекта User пользователя, из которого он создан
                            MAP_USERS.put(user.toString(), user);                                                                // сохранить ссылку на объект User пользователя в HashMap
                            MAP_BRANCHES.get(location).addChild(userBranch);                                                     // добавить Branch пользователя в список children в Branch сервиса
                            FXCollections.sort(children);
                        }
                    }
                }
            }
        };
    }

    /**
     * Перебирает список пользователей и к каждой группе добавляет объект User, у которого
     * location == location группы, а username == null
     */
    private void makeNewList(ObservableList<User> list, int index) {
        ObservableList<User> USER_SUBLIST = FXCollections.observableArrayList();
        int listLength = list.size();
        User userObj = list.get(index);

        if (userObj.getUsername() != null) {
            User newUser = new User(null, userObj.getLocation());
            MAP_LOCATIONS.put(userObj.getLocation(), newUser);
            listOfUsers.add(newUser);
        }

        do {
            USER_SUBLIST.add(list.get(index));
            if (list.get(index).getUsername() != null) {
                MAP_USERS.put(list.get(index).toString(), list.get(index));
            }
            index++;
        }
        while (index < listLength && list.get(index - 1).getLocation().equals(list.get(index).getLocation()));

        // создаётся новый список listOfUsers, в котором есть объекты User и без username, и с username
        listOfUsers.addAll(USER_SUBLIST);

        if (index < listLength) {
            makeNewList(list, index);
        }
    }

    private Branch newBranch(String name) {
        return new Branch(name);
    }

    /**
     * Сохраняет введённые данные в базу и дерево
     */
    public void addLocationToTree(String location) {
        if (!location.isEmpty() && !location.matches(".*\\s+.*")) {
            if (!MAP_LOCATIONS.containsKey(location)) {
                newLocationUser(location);
                selectTreeItem(location);
                StageComposer.setNotifier("success", null, "Новый сервис добавлен в базу.", Duration.millis(2000), 30, 230, 15, null);
            } else {
                StageComposer.setNotifier("error", null, "Такой сервис уже есть в списке!", Duration.millis(2000), 30, 230, 15, null);
            }
        } else {
            StageComposer.setNotifier("error", null, "Укажите название сервиса!", Duration.millis(2000), 30, 230, 15, null);
        }
    }

    public boolean addUserToTree(String location, String username, String password, String otherData) {
        if (!location.isEmpty() && !username.isEmpty() && !password.isEmpty() &&
                !location.matches(".*\\s+.*") && !username.matches(".*\\s+.*") && !password.matches(".*\\s+.*")) {
            // поиск location в MAP_BRANCHES
            if (MAP_BRANCHES.containsKey(location)) {
                // поиск username среди children в Branch
                // если нет, создать новый User и добавить в listOfUsers
                if (!searchChild(MAP_BRANCHES.get(location), username)) {
                    listOfUsers.add(new User(username, location));
                    addUserToDB(location, username, password, otherData);
                    StageComposer.setNotifier("success", null, "Данные успешно добавлены в базу.", Duration.millis(2000), 30, 260, 15, null);
                } else {
                    StageComposer.setNotifier("error", null, "Такой пользователь уже есть в \n" + location + ".", Duration.millis(2000), 60, 230, 15, null);
                }
            } else {
                // нету
                // создать новых User для location и для username и поместить их в listOfUsers
                newLocationUser(location);
                listOfUsers.add(new User(username, location));
                addUserToDB(location, username, password, otherData);
                StageComposer.setNotifier("success", null, "Данные успешно добавлены в базу.", Duration.millis(2000), 30, 260, 15, null);
            }
            selectTreeItem(location);
            return true;
        } else {
            StageComposer.setNotifier("error", null, "Заполните все поля!", Duration.millis(2000), 30, 230, 15, null);
            return false;
        }
    }

    private void newLocationUser(String location) {
        User user = new User(null, location);
        listOfUsers.add(user);
        addLocationToDB(location);
    }

    private void addLocationToDB(String location) {
        try {
            db.addLocationToDB(location);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addUserToDB(String location, String username, String password, String otherData) {
        Runnable task = () -> {
            String[] processed = dispatcher.perform(
                    new EncryptionTask(password, keyWord.getKey()),
                    !otherData.isEmpty() ? new EncryptionTask(otherData, keyWord.getKey()) : null);

            try {
                if (!otherData.isEmpty()) {
                    db.addUserToDB(location, username, processed[0], processed[1]);
                } else {
                    db.addUserToDB(location, username, processed[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        eventExService.execute(task);
    }

    /**
     * Чекбоксы и кнопки возле полей ввода/вывода логина и пароля
     */
    public void saveLocation(String location) {
        Branch selectedItem = treeView.getSelectionModel().getSelectedItem().getValue();
        String oldLocation = selectedItem.getName();
        if (AlertDialog.alert(primaryStage, "Сохранение изменений", "Сохранить изменения?", "warning")) {
            if (!MAP_LOCATIONS.containsKey(location)) {
                User loc = MAP_LOCATIONS.remove(oldLocation);
                loc.setLocation(location);
                db.updateLocation(oldLocation, location);

                for (Branch userBranch : MAP_BRANCHES.get(oldLocation).getChildren()) {
                    User user = MAP_USERS.remove(oldLocation + ":" + userBranch.getName());
                    MAP_USERS.put(user.toString(), user);
                }

                MAP_LOCATIONS.put(loc.getLocation(), loc);
                MAP_BRANCHES.put(location, MAP_BRANCHES.remove(oldLocation));
                StageComposer.setNotifier("success", null, "Изменения сохранены.", Duration.millis(2000), 30, 180, 15, null);
            } else {
                StageComposer.setNotifier("error", null, "Такой сервис уже существует.", Duration.millis(2000), 30, 230, 15, null);
            }
        }
    }

    public void saveUsername(String newUsername) {
        if (!newUsername.isEmpty() && !newUsername.matches(".*\\s+.*")) {
            Branch selectedItem = treeView.getSelectionModel().getSelectedItem().getValue();
            Branch parent = treeView.getSelectionModel().getSelectedItem().getParent().getValue();

            if (AlertDialog.alert(primaryStage, "Сохранение изменений", "Сохранить изменения?", "warning")) {
                if (!searchChild(parent, newUsername)) {
                    try {
                        User selectedUser = MAP_USERS.remove(parent.getName() + ":" + selectedItem.getName());
                        selectedUser.setUsername(newUsername);
                        MAP_USERS.put(selectedUser.toString(), selectedUser);

                        db.updateUsername(selectedUser.getLocation(), tempUsername, newUsername);
                        edit_usernameMain.setText(newUsername);

                        StageComposer.setNotifier("success", null, "Новое имя пользователя \nсохранено в базу.", Duration.millis(2000), 60, 230, 15, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    StageComposer.setNotifier("error", null, "Такой пользователь уже есть в \n" + parent.getName() + ".", Duration.millis(2000), 60, 230, 15, null);
                }
            }
        } else {
            edit_usernameMain.setText(tempUsername);
            StageComposer.setNotifier("warning", null, "Нельзя оставлять пустым!", Duration.millis(2000), 30, 230, 15, null);
        }
    }

    private void savePassword() {

        TreeItem<Branch> username = treeView.getSelectionModel().getSelectedItem();
        TreeItem<Branch> location = username.getParent();

        if (!edit_passwordMain.getText().isEmpty() && !edit_passwordMain.getText().matches(".*\\s+.*")) {
            if (AlertDialog.alert(primaryStage, "Сохранение изменений", "Сохранить изменения?", "warning")) {
                Runnable task = () -> {
                    try {
                        db.updatePassword(location.getValue().getName(), username.getValue().getName(),
                                dispatcher.perform(new EncryptionTask(edit_passwordMain.getText(), keyWord.getKey()))[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                eventExService.execute(task);

                tempPassword = edit_passwordMain.getText();
                StageComposer.setNotifier("success", null, "Новый пароль сохранён в базу.", Duration.millis(2000), 30, 230, 15, null);
            }
        } else {
            edit_passwordMain.setText(tempPassword);
            StageComposer.setNotifier("warning", null, "Нельзя оставлять пустым!", Duration.millis(2000), 30, 230, 15, null);
        }
    }

    private void saveOtherData() {
        TreeItem<Branch> userBranch = treeView.getSelectionModel().getSelectedItem();
        TreeItem<Branch> locationBranch = userBranch.getParent();

        String username = userBranch.getValue().getName();
        String location = locationBranch.getValue().getName();

        String newOtherData = txt_otherData.getText().trim();

        if (newOtherData.equals(tempOtherData)) {
            // не изменилось
            StageComposer.setNotifier("warning", null, "Ничего не изменилось.", Duration.millis(2000), 30, 170, 15, null);
            txt_otherData.setText("");
        } else {
            // изменилось
            if (newOtherData.isEmpty()) {
                // стало пустым
                if (AlertDialog.alert(primaryStage, "Удаление", "Очистить данные?", "warning")) {
                    db.deleteOtherData(location, username);
                    txt_otherData.setText("");
                    StageComposer.setNotifier("success", null, "Изменения сохранены.", Duration.millis(2000), 30, 230, 15, null);
                }
            } else {
                // стало не пустым
                if (tempOtherData.isEmpty()) {
                    // было пустым
                    if (AlertDialog.alert(primaryStage, "Сохранение изменений", "Сохранить изменения?", "warning")) {
                        Runnable task = () -> {
                            try {
                                db.addOtherDataToDB(location, username, dispatcher.perform(new EncryptionTask(newOtherData, keyWord.getKey()))[0]/*AESEncryption.getEncrypted(newOtherData, keyWord.getKey())*/);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                        eventExService.execute(task);
                        StageComposer.setNotifier("success", null, "Изменения сохранены.", Duration.millis(2000), 30, 230, 15, null);
                    }
                } else {
                    // было не пустым
                    if (AlertDialog.alert(primaryStage, "Сохранение изменений", "Сохранить изменения?", "warning")) {
                        Runnable task = () -> {
                            try {
                                db.updateOtherData(location, username, dispatcher.perform(new EncryptionTask(newOtherData, keyWord.getKey()))[0]/*AESEncryption.getEncrypted(newOtherData, keyWord.getKey())*/);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                        eventExService.execute(task);
                        StageComposer.setNotifier("success", null, "Изменения сохранены.", Duration.millis(2000), 30, 230, 15, null);
                    }
                }
            }
        }
        tempOtherData = newOtherData;

    }

    public void copyUsername() {
        copyToClipboard(edit_usernameMain);
    }

    public void copyPassword() {
        copyToClipboard(edit_passwordMain);
    }

    private void copyToClipboard(TextField field) {
        String string = field.getText();
        content.putString(string);
        clipboard.setContent(content);
        StageComposer.setNotifier("success", null, "Данные скопированы в \n     буфер обмена.", Duration.millis(2000), 60, 200, 15, null);
    }

    /**
     * Удаление данных
     */
    @FXML
    public void handleDeleteBtn() {
        if (!treeView.getSelectionModel().isEmpty()) {
            if (AlertDialog.alert(primaryStage, "Удаление", "Выбранные данные будут удалены. Вы подтверждаете удаление?", "recyclebin")) {

                TreeItem<Branch> selectedNode = treeView.getSelectionModel().getSelectedItem();
                TreeItem<Branch> parentNode = selectedNode.getParent();
                String selectedNodeName = selectedNode.getValue().getName();
                String parentNodeName = parentNode.getValue().getName();

                if ((parentNode != rootNode) && (selectedNode.isLeaf())) {
                    listOfUsers.remove(MAP_USERS.get(parentNodeName + ":" + selectedNodeName));
                    StageComposer.setNotifier("success", null, "Пользователь успешно удалён.", Duration.millis(2000), 30, 230, 15, null);
                } else if (parentNode == rootNode) {
                    ObservableList<Branch> children = selectedNode.getValue().getChildren();
                    if (children.size() > 0) {
                        Iterator<Branch> iterator = children.iterator();
                        ObservableList<User> temp = FXCollections.observableArrayList();

                        while (iterator.hasNext()) {
                            Branch branch = iterator.next();
                            temp.add(MAP_USERS.get(selectedNodeName + ":" + branch.getName()));
                        }

                        listOfUsers.removeAll(temp);
                    }
                    listOfUsers.remove(MAP_LOCATIONS.get(selectedNodeName));
                    StageComposer.setNotifier("success", null, "Сервис успешно удалён.", Duration.millis(2000), 30, 200, 15, null);
                }
            }
        } else {
            StageComposer.setNotifier("warning", null, "Ничего не выбрано!", Duration.millis(2000), 30, 170, 15, null);
        }

    }

    private void deleteUser(String location, String username) {
        try {
            db.deleteUser(location, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteLocation(String location) {
        try {
            db.deleteLocation(location);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * проверка состояния чекбокса для пароля. выбран - редактируемое поле
     * пароля, кнопка изменяется на сохранение пароля; не выбран - не
     * редактируемое поле пароля, кнопка копирует содержимое поля пароля.
     */

    private void CheckBoxListener(boolean value) {
        if (value) {
            btn_passwordCopy.setText("Сохранить");
            btn_passwordCopy.setOnAction(e -> {
                try {
                    savePassword();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            btn_passwordCopy.setTooltip(tt_save);
        } else {
            btn_passwordCopy.setText("Копировать");
            btn_passwordCopy.setOnAction(e -> copyPassword());
            btn_passwordCopy.setTooltip(tt_copy);
            edit_passwordMain.setText(tempPassword);
            txt_otherData.setText(tempOtherData);
        }
    }

    private void setVisibility(boolean visible) {
        check_setEdit.setVisible(visible);

        edit_usernameMain.setVisible(visible);
        edit_passwordMain.setVisible(visible);
        txt_otherData.setVisible(visible);

        btn_passwordCopy.setVisible(visible);
        btn_usernameCopy.setVisible(visible);

        lbl_login.setVisible(visible);
        lbl_pass.setVisible(visible);
        lbl_other.setVisible(visible);
        lbl_data.setVisible(visible);
    }

    /**
     * копирует название выделенной ветви в edit_addLocation на панели AddDataPop.fxml
     */
    private void setLocation() {
        try {
            if (treeView.getSelectionModel() != null && treeView.getSelectionModel().selectedItemProperty() != null) {
                if (treeView.getSelectionModel().getSelectedItem() != null) {
                    if (treeView.getSelectionModel().getSelectedItem().getParent() == rootNode) {
                        addDataController.setLocation(treeView.getSelectionModel().getSelectedItem().getValue().getName());
                    } else {
                        addDataController.setLocation(treeView.getSelectionModel().getSelectedItem().getParent().getValue().getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Создаёт Tooltips
     *
     * @param text текст, для вывода в подсказке.
     * @return сформированная подсказка.
     */
    private Tooltip makeToolTip(String text) {
        Tooltip tooltip = new Tooltip(text);
        return tooltip;
    }

    private void selectTreeItem(String location) {
     /*   treeView.getRoot().getChildren().stream().filter(branchToSelect -> branchToSelect.getValue().getName().toLowerCase().startsWith(location.toLowerCase())*//*contains(location)*//*).forEach(branchToSelect -> {
            treeView.getSelectionModel().select(branchToSelect);
            treeView.scrollTo(treeView.getFocusModel().focusedIndexProperty().getValue());
        });*/

        for (TreeItem<Branch> branchToSelect : treeView.getRoot().getChildren()) {
            if (branchToSelect.getValue().getName().toLowerCase().startsWith(location.toLowerCase())) {
                treeView.getSelectionModel().select(branchToSelect);
                treeView.getFocusModel().focus(treeView.getSelectionModel().getSelectedIndex());
                treeView.scrollTo(treeView.getFocusModel().focusedIndexProperty().getValue());
            }
        }
    }

    private boolean searchChild(Branch parent, String childToFind) {
        boolean found = false;
        for (Branch user : parent.getChildren()) {
            if (user.getName().equals(childToFind)) {
                found = true;
                break;
            }
        }
        return found;
    }


    @FXML
    private void handleLogPassChangeMenu() throws IOException {
        Stage stage = new Stage(StageStyle.UNIFIED);
        VerificationController vc = new VerificationController(stage, primaryStage);
        vc.setMain(main);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/VerificationDialog.fxml"));

        loader.setController(vc);
        AnchorPane pane = loader.load();

        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage = StageComposer.setUpStage(stage, pane, "Проверка", 320, 320, 200, 200);

        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        double stageXmiddle = primaryStage.xProperty().getValue() + primaryStage.getWidth() * 0.5;
        double stageYmiddle = primaryStage.yProperty().getValue() + primaryStage.getHeight() * 0.5;

        double x = stageXmiddle > screenWidth ? screenWidth - stage.getWidth() :
                primaryStage.getX() + primaryStage.getWidth() * 0.5 - stage.getWidth() * 0.5;

        double y = stageYmiddle > screenHeight ? screenHeight - stage.getHeight() :
                primaryStage.getY() + primaryStage.getHeight() * 0.5 - stage.getHeight() * 0.5;

        stage.setX(x);
        stage.setY(y);

        final Stage finalStage = stage;
        stage.getScene().setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
                finalStage.close();
            }
        });
    }

    @FXML
    private void handleExit() {
        shutdownExecutor();
        main.closeApplication();
    }

    @FXML
    protected void handleSignOff() throws IOException {
        db.disconnectFromPrivateDB();
        primaryStage.hide();
        main.loadAuthentication();
    }

    public void shutdownExecutor() {
        shutdownAndAwaitTermination(dispatcher.getExecutorService());
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(5, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
