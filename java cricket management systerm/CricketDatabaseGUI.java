import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CricketDatabaseGUI extends JFrame implements ActionListener {
    private JButton insertButton;
    private JButton viewButton;
    private JButton searchButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JTextArea resultArea;
    private Connection connection;

    public CricketDatabaseGUI() {
        super("Cricket Database");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize buttons
        insertButton = createStyledButton("Insert", Color.decode("#ff6e54"), Color.WHITE);
        viewButton = createStyledButton("View", Color.decode("#ff6e54"), Color.WHITE);
        searchButton = createStyledButton("Search", Color.decode("#ff6e54"), Color.WHITE);
        updateButton = createStyledButton("Update", Color.decode("#ff6e54"), Color.WHITE);
        deleteButton = createStyledButton("Delete", Color.decode("#ff6e54"), Color.WHITE);

        // Initialize result area
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setBackground(Color.decode("#2a9d8f"));
        buttonPanel.add(insertButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Create result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add components to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.decode("#e9c46a"));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Set up button actions
        insertButton.addActionListener(this);
        viewButton.addActionListener(this);
        searchButton.addActionListener(this);
        updateButton.addActionListener(this);
        deleteButton.addActionListener(this);

        // Initialize and establish database connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cricket_database", "root", "guessMe123");
            CricketDatabase_create.createTables(connection); // Create tables if not exist
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == insertButton) {
            insert();
        } else if (e.getSource() == viewButton) {
            view();
        } else if (e.getSource() == searchButton) {
            search();
        }else if (e.getSource() == updateButton) {
            update();
        }else if (e.getSource() == deleteButton) {
            delete();
        }
    }

    private void insert() {
        String[] options = {"Player", "Team", "Match", "Umpire"};
        int choice = JOptionPane.showOptionDialog(this, "Select what you want to insert:", "Insert", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                insertPlayer();
                break;
            case 1:
                insertTeam();
                break;
            case 2:
                insertMatch();
                break;
            case 3:
                insertUmpire();
                break;
        }
    }

    private void insertTeam() {
        try {
            // Get team data from user input
            String name = JOptionPane.showInputDialog(this, "Enter team name:");
            int matchesPlayed = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter matches played:"));
            int matchesWon = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter matches won:"));

            // Insert team data into the database
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Teams (name, matches_played, matches_won) VALUES (?, ?, ?)");
            statement.setString(1, name);
            statement.setInt(2, matchesPlayed);
            statement.setInt(3, matchesWon);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            // Show success message if insertion is successful
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Team inserted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert team.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to insert team: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void insertPlayer() {
        try {
            // Get player data from user input
            String name = JOptionPane.showInputDialog(this, "Enter player name:");
            int age = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter player age:"));
            int teamId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter team ID:"));

            // Call the stored procedure to insert player data
            CallableStatement cstmt = connection.prepareCall("{call insert_player(?, ?, ?)}");
            cstmt.setString(1, name);
            cstmt.setInt(2, age);
            cstmt.setInt(3, teamId);
            boolean hasResults = cstmt.execute();

            // Process any results (if applicable)
            // Note: This stored procedure only returns the number of rows affected, so you might not need to process any results here.

            // Close the CallableStatement
            cstmt.close();
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to insert player: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 /* DELIMITER $$

 CREATE PROCEDURE insert_player(
 IN playerName VARCHAR(100),
 IN playerAge INT,
 IN teamId INT
)
 BEGIN
 DECLARE rowsAffected INT;

 -- Insert player data into the Players table
 INSERT INTO Players (name, age, team_id) VALUES (playerName, playerAge, teamId);

 -- Get the number of rows affected by the insert operation
 SET rowsAffected = ROW_COUNT();

 -- Return the number of rows affected
 SELECT rowsAffected AS 'RowsAffected';
 END$$

 DELIMITER ;
*/

    private void insertMatch() {
        try {
            // Get match data from user input
            String date = JOptionPane.showInputDialog(this, "Enter match date (YYYY-MM-DD):");
            String location = JOptionPane.showInputDialog(this, "Enter match location:");
            int team1Id = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter team1 ID:"));
            int team2Id = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter team2 ID:"));
            int umpireId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter umpire ID:"));

            // Insert match data into the database
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Matches (date, location, team1_id, team2_id, umpire_id) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, date);
            statement.setString(2, location);
            statement.setInt(3, team1Id);
            statement.setInt(4, team2Id);
            statement.setInt(5, umpireId);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            // Show success message if insertion is successful
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Match inserted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert match.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to insert match: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertUmpire() {
        try {
            // Get umpire data from user input
            String name = JOptionPane.showInputDialog(this, "Enter umpire name:");
            String nationality = JOptionPane.showInputDialog(this, "Enter umpire nationality:");

            // Insert umpire data into the database
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Umpires (name, nationality) VALUES (?, ?)");
            statement.setString(1, name);
            statement.setString(2, nationality);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            // Show success message if insertion is successful
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Umpire inserted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert umpire.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to insert umpire: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void view() {
        String[] options = {"Players", "Teams", "Matches", "Umpires"};
        int choice = JOptionPane.showOptionDialog(this, "Select what you want to view:", "View", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                view("Players");
                break;
            case 1:
                view("Teams");
                break;
            case 2:
                view("Matches");
                break;
            case 3:
                view("Umpires");
                break;
        }
    }

    private void view(String dataType) {
        try {
            // Create a statement to execute SQL query
            Statement statement = connection.createStatement();
            // Execute SQL query to select all data of given type
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + dataType);

            // Clear the result area
            resultArea.setText("");

            // Iterate through the result set and append data information to the result area
            while (resultSet.next()) {
                switch (dataType) {
                    case "Players":
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        int age = resultSet.getInt("age");
                        int teamId = resultSet.getInt("team_id");
                        resultArea.append("ID: " + id + "\nName: " + name + "\nAge: " + age + "\nTeam ID: " + teamId + "\n\n");
                        break;
                    case "Teams":
                        id = resultSet.getInt("id");
                        name = resultSet.getString("name");
                        int matchesPlayed = resultSet.getInt("matches_played");
                        int matchesWon = resultSet.getInt("matches_won");
                        resultArea.append("ID: " + id + "\n Name: " + name + "\n Matches Played: " + matchesPlayed + "\n Matches Won: " + matchesWon + "\n\n");
                        break;
                    case "Matches":
                        id = resultSet.getInt("id");
                        String date = resultSet.getString("date");
                        String location = resultSet.getString("location");
                        int team1Id = resultSet.getInt("team1_id");
                        int team2Id = resultSet.getInt("team2_id");
                        int umpireId = resultSet.getInt("umpire_id");
                        resultArea.append("ID: " + id + "\n Date: " + date + "\n Location: " + location + "\n Team1 ID: " + team1Id + "\n Team2 ID: " + team2Id + "\n Umpire ID: " + umpireId + "\n\n");
                        break;
                    case "Umpires":
                        id = resultSet.getInt("id");
                        name = resultSet.getString("name");
                        String nationality = resultSet.getString("nationality");
                        resultArea.append("ID: " + id + "\n Name: " + name + "\n Nationality: " + nationality + "\n\n");
                        break;
                }
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to view " + dataType + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void search() {
        String[] options = {"Players", "Teams", "Matches"};
        int choice = JOptionPane.showOptionDialog(this, "Select what you want to search:", "Search", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                searchAndDisplay("Players");
                break;
            case 1:
                searchAndDisplay("Teams");
                break;
            case 2:
                searchAndDisplay("Matches");
                break;
        }
    }

    private void searchAndDisplay(String dataType) {
        String searchQuery;
        switch (dataType) {
            case "Players":
                searchQuery = "Enter player name, player id, or team name:";
                break;
            case "Teams":
                searchQuery = "Enter team name or team id:";
                break;
            case "Matches":
                searchQuery = "Enter match date (YYYY-MM-DD) or team name:";
                break;
            default:
                return;
        }

        String searchCriteria = JOptionPane.showInputDialog(this, searchQuery);
        if (searchCriteria != null && !searchCriteria.isEmpty()) {
            try {
                PreparedStatement statement;
                switch (dataType) {
                    case "Players":
                        statement = connection.prepareStatement("SELECT * FROM Players WHERE name LIKE ? OR id LIKE ? OR team_id IN (SELECT id FROM Teams WHERE name LIKE ?)");
                        statement.setString(1, "%" + searchCriteria + "%");
                        statement.setString(2, "%" + searchCriteria + "%");
                        statement.setString(3, "%" + searchCriteria + "%");
                        break;
                    case "Teams":
                        statement = connection.prepareStatement("SELECT * FROM Teams WHERE name LIKE ? OR id LIKE ?");
                        statement.setString(1, "%" + searchCriteria + "%");
                        statement.setString(2, "%" + searchCriteria + "%");
                        break;
                    case "Matches":
                        statement = connection.prepareStatement("SELECT * FROM Matches WHERE date LIKE ? OR team1_id IN (SELECT id FROM Teams WHERE name LIKE ?) OR team2_id IN (SELECT id FROM Teams WHERE name LIKE ?)");
                        statement.setString(1, "%" + searchCriteria + "%");
                        statement.setString(2, "%" + searchCriteria + "%");
                        statement.setString(3, "%" + searchCriteria + "%");
                        break;
                    default:
                        return;
                }

                ResultSet resultSet = statement.executeQuery();

                resultArea.setText("");
                if (!resultSet.isBeforeFirst()) {
                    resultArea.append("No " + dataType + " found.");
                } else {
                    while (resultSet.next()) {
                        switch (dataType) {
                            case "Players":
                                int id = resultSet.getInt("id");
                                String name = resultSet.getString("name");
                                int age = resultSet.getInt("age");
                                int teamId = resultSet.getInt("team_id");
                                resultArea.append("ID: " + id + "\nName: " + name + "\nAge: " + age + "\nTeam ID: " + teamId + "\n\n");
                                break;
                            case "Teams":
                                id = resultSet.getInt("id");
                                name = resultSet.getString("name");
                                int matchesPlayed = resultSet.getInt("matches_played");
                                int matchesWon = resultSet.getInt("matches_won");
                                resultArea.append("ID: " + id + "\n Name: " + name + "\n Matches Played: " + matchesPlayed + "\n Matches Won: " + matchesWon + "\n\n");
                                break;
                            case "Matches":
                                id = resultSet.getInt("id");
                                String date = resultSet.getString("date");
                                String location = resultSet.getString("location");
                                int team1Id = resultSet.getInt("team1_id");
                                int team2Id = resultSet.getInt("team2_id");
                                int umpireId = resultSet.getInt("umpire_id");
                                resultArea.append("ID: " + id + "\n Date: " + date + "\n Location: " + location + "\n Team1 ID: " + team1Id + "\n Team2 ID: " + team2Id + "\n Umpire ID: " + umpireId + "\n\n");
                                break;
                        }
                    }
                }

                resultSet.close();
                statement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to search " + dataType + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void update() {
        String[] options = {"Player", "Team", "Match"};
        int choice = JOptionPane.showOptionDialog(this, "Select what you want to update:", "Update", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                updatePlayer();
                break;
            case 1:
                updateTeam();
                break;
            case 2:
                updateMatch();
                break;
        }
    }

    private void updatePlayer() {
        try {
            // Get player ID from user input
            int playerId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter player ID:"));

            // Ask what information the user wants to update
            String[] options = {"Name", "Age", "Team ID"};
            int choice = JOptionPane.showOptionDialog(this, "Select what you want to update:", "Update Player", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            String columnToUpdate;
            switch (choice) {
                case 0:
                    columnToUpdate = "name";
                    break;
                case 1:
                    columnToUpdate = "age";
                    break;
                case 2:
                    columnToUpdate = "team_id";
                    break;
                default:
                    return;
            }

            // Get the new value from user input
            String newValue = JOptionPane.showInputDialog(this, "Enter new value for " + columnToUpdate + ":");

            // Update player data in the database
            PreparedStatement statement = connection.prepareStatement("UPDATE Players SET " + columnToUpdate + "=? WHERE id=?");
            statement.setString(1, newValue);
            statement.setInt(2, playerId);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            // Show success message if update is successful
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Player updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update player.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update player: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTeam() {
        try {
            // Get team ID from user input
            int teamId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter team ID:"));

            // Ask what information the user wants to update
            String[] options = {"Name", "Matches Played", "Matches Won"};
            int choice = JOptionPane.showOptionDialog(this, "Select what you want to update:", "Update Team", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            String columnToUpdate;
            switch (choice) {
                case 0:
                    columnToUpdate = "name";
                    break;
                case 1:
                    columnToUpdate = "matches_played";
                    break;
                case 2:
                    columnToUpdate = "matches_won";
                    break;
                default:
                    return;
            }

            // Get the new value from user input
            String newValue = JOptionPane.showInputDialog(this, "Enter new value for " + columnToUpdate + ":");

            // Update team data in the database
            PreparedStatement statement = connection.prepareStatement("UPDATE Teams SET " + columnToUpdate + "=? WHERE id=?");
            statement.setString(1, newValue);
            statement.setInt(2, teamId);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            // Show success message if update is successful
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Team updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update team.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update team: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMatch() {
        try {
            // Get match ID from user input
            int matchId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter match ID:"));

            // Ask what information the user wants to update
            String[] options = {"Date", "Location", "Team1 ID", "Team2 ID", "Umpire ID"};
            int choice = JOptionPane.showOptionDialog(this, "Select what you want to update:", "Update Match", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            String columnToUpdate;
            switch (choice) {
                case 0:
                    columnToUpdate = "date";
                    break;
                case 1:
                    columnToUpdate = "location";
                    break;
                case 2:
                    columnToUpdate = "team1_id";
                    break;
                case 3:
                    columnToUpdate = "team2_id";
                    break;
                case 4:
                    columnToUpdate = "umpire_id";
                    break;
                default:
                    return;
            }

            // Get the new value from user input
            String newValue = JOptionPane.showInputDialog(this, "Enter new value for " + columnToUpdate + ":");

            // Update match data in the database
            PreparedStatement statement = connection.prepareStatement("UPDATE Matches SET " + columnToUpdate + "=? WHERE id=?");
            statement.setString(1, newValue);
            statement.setInt(2, matchId);
            int rowsAffected = statement.executeUpdate();
            statement.close();

            // Show success message if update is successful
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Match updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update match.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update match: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /*
    CREATE TRIGGER update_team_statistics AFTER INSERT ON Matches
    FOR EACH ROW
    BEGIN
     UPDATE Teams
     SET matches_played = matches_played + 1
     WHERE id = NEW.team1_id OR id = NEW.team2_id;
    END;
    */
    private void delete() {
        String[] options = {"Player", "Match", "Team"};
        int choice = JOptionPane.showOptionDialog(this, "Select what you want to delete:", "Delete", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                deletePlayer();
                break;
            case 1:
                deleteMatch();
                break;
            case 2:
                deleteTeam();
                break;
        }
    }

    private void deletePlayer() {
        try {
            // Ask user for deletion criteria
            String[] criteriaOptions = {"Player ID", "Player Name"};
            int criteriaChoice = JOptionPane.showOptionDialog(this, "Select deletion criteria:", "Delete Player", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, criteriaOptions, criteriaOptions[0]);

            String columnName;
            String inputValue;
            switch (criteriaChoice) {
                case 0:
                    columnName = "id";
                    inputValue = JOptionPane.showInputDialog(this, "Enter player ID:");
                    break;
                case 1:
                    columnName = "name";
                    inputValue = JOptionPane.showInputDialog(this, "Enter player name:");
                    break;
                default:
                    return;
            }

            // Get player details for confirmation
            PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM Players WHERE " + columnName + "=?");
            selectStatement.setString(1, inputValue);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Display player details for reconfirmation
                int playerId = resultSet.getInt("id");
                String playerName = resultSet.getString("name");
                int playerAge = resultSet.getInt("age");
                int teamId = resultSet.getInt("team_id");

                int confirmDelete = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this player?\n\nID: " + playerId + "\nName: " + playerName + "\nAge: " + playerAge + "\nTeam ID: " + teamId, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirmDelete == JOptionPane.YES_OPTION) {
                    // Delete player from the database
                    PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM Players WHERE " + columnName + "=?");
                    deleteStatement.setString(1, inputValue);
                    int rowsAffected = deleteStatement.executeUpdate();
                    deleteStatement.close();

                    // Show success message if deletion is successful
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Player deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete player.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Player not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete player: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteMatch() {
        try {
            // Ask user for deletion criteria
            String[] criteriaOptions = {"Match ID"};
            int criteriaChoice = JOptionPane.showOptionDialog(this, "Select deletion criteria:", "Delete Match", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, criteriaOptions, criteriaOptions[0]);

            String columnName;
            String inputValue;
            switch (criteriaChoice) {
                case 0:
                    columnName = "id";
                    inputValue = JOptionPane.showInputDialog(this, "Enter match ID:");
                    break;
                default:
                    return;
            }

            // Get match details for confirmation
            PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM Matches WHERE " + columnName + "=?");
            selectStatement.setString(1, inputValue);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Display match details for reconfirmation
                int matchId = resultSet.getInt("id");
                String matchDate = resultSet.getString("date");
                String matchLocation = resultSet.getString("location");
                int team1Id = resultSet.getInt("team1_id");
                int team2Id = resultSet.getInt("team2_id");
                int umpireId = resultSet.getInt("umpire_id");

                int confirmDelete = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this match?\n\nMatch ID: " + matchId + "\nDate: " + matchDate + "\nLocation: " + matchLocation + "\nTeam1 ID: " + team1Id + "\nTeam2 ID: " + team2Id + "\nUmpire ID: " + umpireId, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirmDelete == JOptionPane.YES_OPTION) {
                    // Delete match from the database
                    PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM Matches WHERE " + columnName + "=?");
                    deleteStatement.setString(1, inputValue);
                    int rowsAffected = deleteStatement.executeUpdate();
                    deleteStatement.close();

                    // Show success message if deletion is successful
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Match deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete match.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Match not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete match: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTeam() {
        try {
            // Ask user for deletion criteria
            String[] criteriaOptions = {"Team ID", "Team Name"};
            int criteriaChoice = JOptionPane.showOptionDialog(this, "Select deletion criteria:", "Delete Team", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, criteriaOptions, criteriaOptions[0]);

            String columnName;
            String inputValue;
            switch (criteriaChoice) {
                case 0:
                    columnName = "id";
                    inputValue = JOptionPane.showInputDialog(this, "Enter team ID:");
                    break;
                case 1:
                    columnName = "name";
                    inputValue = JOptionPane.showInputDialog(this, "Enter team name:");
                    break;
                default:
                    return;
            }

            // Get team details for confirmation
            PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM Teams WHERE " + columnName + "=?");
            selectStatement.setString(1, inputValue);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                // Display team details for reconfirmation
                int teamId = resultSet.getInt("id");
                String teamName = resultSet.getString("name");
                int matchesPlayed = resultSet.getInt("matches_played");
                int matchesWon = resultSet.getInt("matches_won");

                int confirmDelete = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this team?\n\nTeam ID: " + teamId + "\nName: " + teamName + "\nMatches Played: " + matchesPlayed + "\nMatches Won: " + matchesWon, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirmDelete == JOptionPane.YES_OPTION) {
                    // Delete team from the database
                    PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM Teams WHERE " + columnName + "=?");
                    deleteStatement.setString(1, inputValue);
                    int rowsAffected = deleteStatement.executeUpdate();
                    deleteStatement.close();

                    // Show success message if deletion is successful
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Team deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete team.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Team not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete team: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CricketDatabaseGUI().setVisible(true);
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}