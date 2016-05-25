package passwordProtector.dataProcessing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import passwordProtector.dataModel.User;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
public class DBHelper {
    private static DBHelper instance;
    private String dbPath, dbName, credentials;
    private Connection connectionPublic, connectionPrivate;

    private DBHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DBHelper getInstance() {
        if (instance == null) {
            instance = new DBHelper();
        }
        return instance;
    }

    public void setDbPath(String path) {
        dbPath = path;
    }

    public void setPublicDbPath(String path) {
        this.credentials = path + "credentials.db";
    }

    public void setDbName(String dbName) throws UnsupportedEncodingException {
        this.dbName = dbPath + new String(dbName.getBytes("UTF-8"), "UTF-8") + ".db";
    }

    public void connectToPrivateDB() {
        try {
            connectionPrivate = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            connectionPrivate.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connectToPublicDB() {
        try {
            connectionPublic = DriverManager.getConnection("jdbc:sqlite:" + credentials);
            connectionPublic.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromPrivateDB() {
        disconnect(connectionPrivate);
    }

    public void disconnectFromPublicDB() {
        disconnect(connectionPublic);
    }

    private void disconnect(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTablesPublic() {
        PreparedStatement statement = null;
        String users = "CREATE TABLE IF NOT EXISTS 'users' ('user_id' integer PRIMARY KEY AUTOINCREMENT, 'username' text NOT NULL UNIQUE, 'password' text NOT NULL, 'salt' text NOT NULL)";
        createDB(connectionPublic, statement, users);
    }

    public void createTablesPrivate() {
        PreparedStatement statement = null;

        String locations = "CREATE TABLE IF NOT EXISTS 'locations' (" +
                "'location_id'  integer PRIMARY KEY AUTOINCREMENT, " +
                "'location' text NOT NULL UNIQUE)";

        String users = "CREATE TABLE IF NOT EXISTS 'users'(" +
                "'user_id' integer PRIMARY KEY AUTOINCREMENT, " +
                "'username' text NOT NULL, " +
                "'password' text NOT NULL, " +
                "'location_id' integer NOT NULL, " +
                "FOREIGN KEY (location_id) REFERENCES locations (location_id), " +
                "UNIQUE (username, location_id))";

        String other_data = "CREATE TABLE IF NOT EXISTS 'other_data'(" +
                "'user_id' integer, " +
                "'data' text NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (user_id))";

        String trigger = "CREATE TRIGGER IF NOT EXISTS deleteOtherData " +
                "BEFORE DELETE " +
                "ON users " +
                "BEGIN " +
                "DELETE FROM other_data WHERE user_id = OLD.user_id; " +
                "END";

        createDB(connectionPrivate, statement, locations);
        createDB(connectionPrivate, statement, users);
        createDB(connectionPrivate, statement, other_data);
        createDB(connectionPrivate, statement, trigger);
    }

    private void createDB(Connection connection, PreparedStatement statement, String sql) {
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**********
     * Insert data
     ************/

    public void insertPublicUser(String username, String password, String salt) {
        String statement = "INSERT INTO users (username, password, salt) VALUES (?, ?, ?)";
        prepareThreeParameters(statement, connectionPublic, username, password, salt);
    }

    public void addLocationToDB(String location) {
        String statement = "INSERT OR IGNORE INTO locations (location) VALUES (?)";
        prepareOneParameter(statement, connectionPrivate, location);
    }

    /*
     * Извлекает location_id из таблицы
     * locations по заданному location и
     * помещает username и password в
     * таблицу users
     */
    public void addUserToDB(String location, String username, String password, String otherData) {
        addUserToDB(location, username, password);
        addOtherDataToDB(location, username, otherData);
    }

    public void addUserToDB(String location, String username, String password) {
        String statement = "INSERT OR IGNORE INTO users (username, password, location_id) SELECT ?, ?, location_id FROM locations WHERE location = ?";
        prepareThreeParameters(statement, connectionPrivate, username, password, location);
    }

    public void addOtherDataToDB(String location, String username, String otherData) {
        String statement = "INSERT INTO other_data(user_id, data) SELECT user_id, ? FROM users WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?)";
        prepareThreeParameters(statement, connectionPrivate, otherData, username, location);
    }

    public ArrayList<DBUser> getAllUserData() {
        ArrayList<DBUser> array = new ArrayList<DBUser>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connectionPrivate.prepareStatement("SELECT users.user_id, users.password, other_data.data FROM users LEFT JOIN other_data ON users.user_id = other_data.user_id");
            rs = statement.executeQuery();
            connectionPrivate.commit();

            while (rs.next()) {
                array.add(new DBUser(Integer.parseInt(rs.getString("user_id")), rs.getString("password"), rs.getString("data")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsStatement(statement, rs);
        }
        return array;
    }


    /**
     * Извлекает отсортированный по location список пользователей из базы в формате User(username, location),
     * где
     * username - имя учётной записи, используемой в
     * location - неком сервисе.
     *
     * @return список всех пользователей
     *********************/

    public ObservableList<User> getUserDataAsList() {
        ObservableList<User> listWithUsers = FXCollections.observableArrayList();

        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connectionPrivate.prepareStatement("SELECT users.username, locations.location FROM locations LEFT JOIN users ON locations.location_id = users.location_id ORDER BY location");
            rs = statement.executeQuery();
            connectionPrivate.commit();

            while (rs.next()) {
                listWithUsers.add(new User(rs.getString("username"), rs.getString("location")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            closeRsStatement(statement, rs);
        }
        return listWithUsers;
    }

    /*************************/

    public String[] getUserData(String location, String username) {
        //long timeMillis = System.currentTimeMillis();
        String[] users = new String[3];
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connectionPrivate.prepareStatement(
                    "SELECT users.username, users.password, other_data.data " +
                    "FROM users LEFT JOIN other_data ON users .user_id= other_data.user_id " +
                    "WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?)");
            statement.setString(1, username);
            statement.setString(2, location);
            rs = statement.executeQuery();
            connectionPrivate.commit();

            while (rs.next()) {
                users[0] = rs.getString("username");
                users[1] = rs.getString("password");
                users[2] = rs.getString("data");
            }

            //long after = System.currentTimeMillis();
            //System.out.println("Time to extract: " + (after - timeMillis));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsStatement(statement, rs);
        }
        return users;
    }

    public boolean doesUserExist(String username) {
        boolean exists = false;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connectionPublic.prepareStatement("SELECT username FROM users WHERE username = ?");
            statement.setString(1, username);
            rs = statement.executeQuery();
            connectionPublic.commit();

            while (rs.next()) {
                if (rs.getString("username") != null) {
                    exists = true;
                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsStatement(statement, rs);

        }
        return exists;
    }

    public boolean getUserValidation(String username, String password) {
        boolean let_in = false;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connectionPublic.prepareStatement("SELECT username, password FROM users WHERE username = ?");
            statement.setString(1, username);
            rs = statement.executeQuery();
            connectionPublic.commit();

            while (rs.next()) {
                if (rs.getString("username") != null) {
                    let_in = PasswordHash.validatePassword(password, rs.getString("password"));
                }
            }

        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            closeRsStatement(statement, rs);
        }

        return let_in;
    }

    public String getUserSalt(String username) {
        String salt = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {

            statement = connectionPublic.prepareStatement("SELECT  salt FROM users WHERE username = ?");
            statement.setString(1, username);
            rs = statement.executeQuery();
            connectionPublic.commit();

            while (rs.next()) {
                salt = rs.getString("salt");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsStatement(statement, rs);
        }
        return salt;
    }

    /***************
     * Update data
     ********************/

    public void updateLocation(String oldLocation, String newLocation) {
        String statement = "UPDATE locations SET location = ? WHERE location = ?";
        prepareTwoParameters(statement, connectionPrivate, newLocation, oldLocation);
    }

    /*
     * Извлекает location_id из таблицы
     * locations по заданному location
     * и изменяет значение username
     * на new_username
     */
    public void updateUsername(String location, String oldUsername, String newUsername) {
        String statement = "UPDATE users SET username = ? WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?)";
        prepareThreeParameters(statement, connectionPrivate, newUsername, oldUsername, location);
    }

    /*
     * Извлекает location_id из таблицы
     * locations по заданному location
     * изменяет значение password в строке с полученым location_id и данным username
     */
    public void updatePassword(String location, String username, String newPassword) {
        String statement = "UPDATE users SET password = ? WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?)";
        prepareThreeParameters(statement, connectionPrivate, newPassword, username, location);
    }

    public void updateOtherData(String location, String username, String otherData) {
        String statement = "UPDATE other_data SET data = ? WHERE user_id = (SELECT user_id FROM users WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?))";
        prepareThreeParameters(statement, connectionPrivate, otherData, username, location);
    }

    public void changePublicUserData(String oldUsername, String newUsername, String newPassword, String newSalt) {
        String statement = "UPDATE users SET username = ?, password = ?, salt = ? WHERE username = ?";
        prepareFourParameters(statement, connectionPublic, newUsername, newPassword, newSalt, oldUsername);
    }

    public void changePublicUsername(String oldUsername, String newUsername, String newSalt) {
        String statement = "UPDATE users SET username = ?, salt = ? WHERE username = ?";
        prepareThreeParameters(statement, connectionPublic, newUsername, newSalt, oldUsername);
    }

    public void changePublicPassword(String oldUsername, String newPassword, String newSalt) {
        String statement = "UPDATE users SET password = ?, salt = ? WHERE username = ?";
        prepareThreeParameters(statement, connectionPublic, newPassword, newSalt, oldUsername);
    }

    private void prepareOneParameter(String sql, Connection connection, String first) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, first);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    private void prepareTwoParameters(String sql, Connection connection, String first, String second) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, first);
            statement.setString(2, second);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    private void prepareThreeParameters(String sql, Connection connection, String first, String second, String third) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, first);
            statement.setString(2, second);
            statement.setString(3, third);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    private void prepareFourParameters(String sql, Connection connection, String first, String second, String third, String fourth) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, first);
            statement.setString(2, second);
            statement.setString(3, third);
            statement.setString(4, fourth);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public void updateReEncrypted(ArrayList<DBUser> list) {
        PreparedStatement statementPass = null;
        PreparedStatement statementData = null;
        int batchSize = 50;
        int count = 0;
        try {
            statementPass = connectionPrivate.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?");
            statementData = connectionPrivate.prepareStatement("UPDATE other_data SET data = ? WHERE user_id = ?");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (DBUser entry : list) {
            count++;
            try {
                statementPass.setString(1, entry.getPassword());
                statementPass.setInt(2, entry.getUserId());
                statementPass.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (entry.getOtherData() != null) {
                try {
                    statementData.setString(1, entry.getOtherData());
                    statementData.setInt(2, entry.getUserId());
                    statementData.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }


            if (count % batchSize == 0) {
                try {
                    statementPass.executeBatch();
                    statementData.executeBatch();
                    connectionPrivate.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            statementPass.executeBatch();
            statementData.executeBatch();
            connectionPrivate.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statementPass);
            closeStatement(statementData);
        }
    }


    /*__________ Delete data ___________*/

    /*
     * Извлекает location_id из таблицы
     * locations по заданному location
     * для проверки количества таких location_id
     * в таблице users, чтобы убедиться
     * в отсутствии записей в таблице users,
     * связанных с location в таблице
     * locations, после чего удаляет эту location из таблицы locations
     */

    public int deleteLocation(String location) {
        int id_count = 0;

        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = connectionPrivate.prepareStatement("SELECT COUNT(location_id) FROM users WHERE location_id = (SELECT location_id FROM locations WHERE location = ?)");
            statement.setString(1, location);
            rs = statement.executeQuery();
            connectionPrivate.commit();

            while (rs.next()) {
                id_count = rs.getInt(1);

                if (id_count == 0) {
                    statement = connectionPrivate.prepareStatement("DELETE FROM locations WHERE location = ?");
                    statement.clearParameters();
                    statement.setString(1, location);
                    statement.executeUpdate();
                    connectionPrivate.commit();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsStatement(statement, rs);
        }
        return id_count;
    }

    /*
     * Извлекает location_id из таблицы
     * locations по заданному location
     * и удаляет username из таблицы users,
     * соответствующий этому location_id
     */
    public void deleteUser(String location, String username) {
        String statement = "DELETE FROM users WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?)";
        prepareTwoParameters(statement, connectionPrivate, username, location);
    }

    public void deleteOtherData(String location, String username) {
        String statement = "DELETE FROM other_data WHERE user_id = (SELECT user_id FROM users WHERE username = ? AND location_id = (SELECT location_id FROM locations WHERE location = ?))";
        prepareTwoParameters(statement, connectionPrivate, username, location);
    }

    private void closeStatement(PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeRsStatement(Statement statement, ResultSet rs) {
        try {
            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
