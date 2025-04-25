package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLDataBaseConnector {
    private Connection connection;

    public void connect(){
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/SCP/database.db");
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                ");";
        try (Statement stmt = connection.createStatement()){
            stmt.execute(sql);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            if (connection != null){
                connection.close();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
