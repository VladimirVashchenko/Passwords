package passwordProtector.dataModel;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Administrator on 28.12.2015.
 */
public class User {
    private final StringProperty username;
    private final StringProperty location;

    public User (){
        this(null, null);
    }

    public User(String username , String location) {
        this.username = new SimpleStringProperty(username);
        this.location = new SimpleStringProperty(location);
    }


    public StringProperty usernameProperty() {
        return username;
    }

    public String getUsername() {
        return username.getValue();
    }

    public void setUsername(String name) {
        this.username.setValue(name);
    }

    public StringProperty locationProperty() {
        return location;
    }

    public String getLocation() {
        return location.getValue();
    }

    public void setLocation(String location) {
        this.location.setValue(location);
    }
    @Override
    public String toString() {
        return getLocation() +":"+ getUsername();
    }

}

