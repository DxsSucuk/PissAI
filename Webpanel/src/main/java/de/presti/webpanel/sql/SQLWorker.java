package de.presti.webpanel.sql;

import de.presti.webpanel.WebpanelApplication;

import java.sql.*;
import java.util.*;

/**
 * A Class to actually handle the SQL data.
 * Used to provide Data from the Database and to save Data into the Database.
 * <p>
 * Constructor to create a new Instance of the SQLWorker with a ref to the SQL-Connector.
 *
 * @param sqlConnector an Instance of the SQL-Connector to retrieve the data from.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve", "unused", "SingleStatementInBlock"})
public record SQLWorker(SQLConnector sqlConnector) {

    //region URL-Entries

    public boolean urlEntryExists(String url) {
        try (ResultSet resultSet = querySQL("SELECT * FROM `URL` WHERE URL = ?", url)) {
            return resultSet != null && resultSet.next();
        } catch (Exception ignored) {}
        return false;
    }

    public void addEntry(String url, String person) {
        if (!urlEntryExists(url)) {
            querySQL("INSERT INTO `URL`(Id, URL, PERSON) VALUES (NULL, ?, ?)", url, person);
        }
    }

    public int getEntryCount() {
        try (ResultSet resultSet = querySQL("SELECT count(*) FROM `URL`")) {
            if (resultSet != null && resultSet.next()) return resultSet.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }

    //endregion

    //region Users

    public boolean doesTokenExist(String token) {
        try (ResultSet resultSet = querySQL("SELECT * FROM `USERS` WHERE TOKEN = ?", token)) {
            return resultSet != null && resultSet.next();
        } catch (Exception ignored) {}
        return false;
    }

    public void createNewUser(String token) {
        if (!doesTokenExist(token)) {
            querySQL("INSERT INTO `USERS`(Id, TOKEN, AMOUNT) VALUES (NULL, ?, ?)", token, 1);
        }
    }

    public void updateUser(String token) {
        if (doesTokenExist(token)) {
            querySQL("UPDATE `USERS` SET AMOUNT = ? WHERE TOKEN = ?",getImageCountOfUser(token) + 1, token);
        }
    }

    public int getImageCountOfUser(String token) {
        if (doesTokenExist(token)) {
            try (ResultSet resultSet = querySQL("SELECT * FROM `USERS` WHERE TOKEN = ?", token)) {
                if (resultSet != null && resultSet.next()) return resultSet.getInt("AMOUNT");
            } catch (Exception ignored) {}
            return 0;
        }
        return 0;
    }

    public int getUserCount() {
        try (ResultSet resultSet = querySQL("SELECT count(*) FROM `USERS`")) {
            if (resultSet != null && resultSet.next()) return resultSet.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }

    //endregion

    //region Utility

    /**
     * Send an SQL-Query to SQL-Server and get the response.
     *
     * @param sqlQuery    the SQL-Query.
     * @param objcObjects the Object in the Query.
     * @return The Result from the SQL-Server.
     */
    public ResultSet querySQL(String sqlQuery, Object... objcObjects) {
        if (!sqlConnector.IsConnected()) return null;

        try (PreparedStatement preparedStatement = sqlConnector.getConnection().prepareStatement(sqlQuery)) {
            int index = 1;

            for (Object obj : objcObjects) {
                if (obj instanceof String) {
                    preparedStatement.setObject(index++, obj, Types.VARCHAR);
                } else if (obj instanceof Blob) {
                    preparedStatement.setObject(index++, obj, Types.BLOB);
                } else if (obj instanceof Integer) {
                    preparedStatement.setObject(index++, obj, Types.INTEGER);
                } else if (obj instanceof Long) {
                    preparedStatement.setObject(index++, obj, Types.BIGINT);
                } else if (obj instanceof Float) {
                    preparedStatement.setObject(index++, obj, Types.FLOAT);
                } else if (obj instanceof Double) {
                    preparedStatement.setObject(index++, obj, Types.DOUBLE);
                } else if (obj instanceof Boolean) {
                    preparedStatement.setObject(index++, obj, Types.BOOLEAN);
                }
            }

            if (sqlQuery.startsWith("SELECT")) {
                return preparedStatement.executeQuery();
            } else {
                preparedStatement.executeUpdate();
                return null;
            }
        } catch (Exception exception) {
            if (exception instanceof SQLNonTransientConnectionException) {
                WebpanelApplication.getInstance().getLogger().severe("Couldn't send Query to SQL-Server, most likely a connection Issue.\n" + exception.getMessage());
                sqlConnector.connectToSQLServer();
            } else {
                WebpanelApplication.getInstance().getLogger().severe("Couldn't send Query to SQL-Server ( " + sqlQuery + " )\n" + exception.getMessage());
            }
        }

        return null;
    }

    //endregion
}
