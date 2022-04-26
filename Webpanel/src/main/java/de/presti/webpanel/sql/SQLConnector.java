package de.presti.webpanel.sql;

import de.presti.webpanel.WebpanelApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A "Connector" Class which connect with the used Database Server.
 * Used to manage the connection between Server and Client.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class SQLConnector {

    // Various String that keep connection information to use for a connection.
    private final String databaseUser,
            databaseName,
            databasePassword,
            databaseServerIP;

    // The port of the Server.
    private final int databaseServerPort;

    // An Instance of the actual Java SQL Connection.
    private Connection connection;

    // An Instance of the SQL-Worker which works with the Data in the Database.
    private final SQLWorker sqlWorker;

    // A HashMap with every Table Name as key and the values as value.
    private final HashMap<String, String> tables = new HashMap<>();

    /**
     * Constructor with the needed data to open an SQL connection.
     * @param databaseUser the Database Username
     * @param databaseName the Database name
     * @param databasePassword the Database User password
     * @param databaseServerIP the Address of the Database Server.
     * @param databaseServerPort the Port of the Database Server.
     */
    public SQLConnector(String databaseUser, String databaseName, String databasePassword, String databaseServerIP, int databaseServerPort) {
        this.databaseUser = databaseUser;
        this.databaseName = databaseName;
        this.databasePassword = databasePassword;
        this.databaseServerIP = databaseServerIP;
        this.databaseServerPort = databaseServerPort;
        connectToSQLServer();
        createTables();

        sqlWorker = new SQLWorker(this);
    }

    /**
     * Try to open a connection to the SQL Server with the given data.
     */
    public void connectToSQLServer() {
        WebpanelApplication.getInstance().getLogger().info("Connecting to SQl.");
        // Check if there is already an open Connection.
        if (IsConnected()) {
            try {
                // Close if there is and notify.
                connection.close();
                WebpanelApplication.getInstance().getLogger().info("Service (MariaDB) has been stopped.");
            } catch (Exception ignore) {
                // Notify if there was an error.
                WebpanelApplication.getInstance().getLogger().severe("Service (MariaDB) couldn't be stopped.");
            }
        }

        try {
            // Create a new Connection by using the SQL DriverManager and the MariaDB Java Driver and notify if successful.
            connection = DriverManager.getConnection("jdbc:mariadb://" + databaseServerIP + ":" + databaseServerPort + "/" + databaseName + "?autoReconnect=true", databaseUser, databasePassword);
            WebpanelApplication.getInstance().getLogger().info("Service (MariaDB) has been started. Connection was successful.");
        } catch (Exception exception) {
            // Notify if there was an error.
            WebpanelApplication.getInstance().getLogger().severe("Service (MariaDB) couldn't be started. Connection was unsuccessful.\n" + exception.getMessage());
        }
    }

    /**
     * Create Tables in the Database if they aren't already set.
     */
    public void createTables() {

        // Check if there is an open Connection if not, skip.
        if (!IsConnected()) return;

        // Registering the tables and values.
        tables.put("URL", "(Id int NOT NULL AUTO_INCREMENT, URL VARCHAR(500) NOT NULL, PERSON VARCHAR(50) NOT NULL, PRIMARY KEY(Id))");

        // Iterating through all table presets.
        for (Map.Entry<String, String> entry : tables.entrySet()) {

            // Create a Table based on the key.
            try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + entry.getKey() + entry.getValue())) {
                ps.executeQuery();
            } catch (SQLException exception) {

                // Notify if there was an error.
                WebpanelApplication.getInstance().getLogger().severe("Couldn't create " + entry.getKey() + " Table.\n"+ exception.getMessage());
            }
        }


    }

    /**
     * Check if there is an open connection to the Database Server.
     * @return boolean If the connection is opened.
     */
    public boolean IsConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (Exception ignore) {}

        return false;
    }

    /**
     * Call to close the current Connection.
     */
    public void close() {
        // Check if there is already an open Connection.
        if (IsConnected()) {
            try {
                // Close if there is and notify.
                connection.close();
                WebpanelApplication.getInstance().getLogger().info("Service (MariaDB) has been stopped.");
            } catch (Exception ignore) {
                // Notify if there was an error.
                WebpanelApplication.getInstance().getLogger().severe("Service (MariaDB) couldn't be stopped.");
            }
        }
    }

    /**
     * Retrieve an Instance of the SQL-Connection.
     * @return Connection Instance of te SQL-Connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Retrieve an Instance of the SQL-Worker to work with the Data.
     * @return {@link SQLWorker} the Instance saved in this SQL-Connector.
     */
    public SQLWorker getSqlWorker() {
        return sqlWorker;
    }

    /**
     * Retrieve a list with all Tables and it values.
     * @return {@link HashMap} with all Tables as Key and all values as value.
     */
    public HashMap<String, String> getTables() { return tables; }
}