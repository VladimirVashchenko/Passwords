package passwordProtector.dataProcessing;


/**
 * Created by Administrator on 19.05.2016.
 */
public class DBUser {
    private int userId;
    private String password;
    private String otherData;

    public DBUser(int user_id, String password, String otherData) {
        this.userId = user_id;
        this.password = password;
        this.otherData = otherData;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtherData() {
        return otherData;
    }

    public void setOtherData(String otherData) {
        this.otherData = otherData;
    }
}
