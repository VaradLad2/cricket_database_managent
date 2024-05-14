import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CricketDatabase_create {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/cricket_database";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "guessMe123";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            createTables(connection);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void createTables(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        // Create Teams table
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Teams (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL UNIQUE," +
                "matches_played INT NOT NULL DEFAULT 0," +
                "matches_won INT NOT NULL DEFAULT 0" +
                ")");

        // Create Players table
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Players (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "age INT NOT NULL," +
                "team_id INT," +
                "matches_played INT NOT NULL DEFAULT 0," +
                "FOREIGN KEY (team_id) REFERENCES Teams(id)" +
                ")");

        // Create Umpires table
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Umpires (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "nationality VARCHAR(255) NOT NULL" +
                ")");

        // Create Matches table
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Matches (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "date DATE NOT NULL," +
                "location VARCHAR(255) NOT NULL," +
                "team1_id INT," +
                "team2_id INT," +
                "umpire_id INT," +
                "FOREIGN KEY (team1_id) REFERENCES Teams(id)," +
                "FOREIGN KEY (team2_id) REFERENCES Teams(id)," +
                "FOREIGN KEY (umpire_id) REFERENCES Umpires(id)" +
                ")");

        statement.close();

        System.out.println("Tables created successfully.");
    }
}
