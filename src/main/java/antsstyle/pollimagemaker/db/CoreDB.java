/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.db;

import antsstyle.pollimagemaker.datastructures.PollRowHolder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class CoreDB {

    private static final Logger LOGGER = LogManager.getLogger();
    private static BasicDataSource connectionPool = new BasicDataSource();

    /**
     * General error code.
     */
    public static final int DB_ERROR = -1;
    /**
     * Error code, when an insert or update fails due to trying to add a duplicate value to a UNIQUE column.
     */
    public static final int DUPLICATE_ERROR = -2;
    /**
     * Error code, for when ArtPoster wasn't given the right parameters and didn't execute the DB query as a result.
     */
    public static final int INPUT_ERROR = -3;

    private static final String CREATE_IMAGE_TABLE = "CREATE TABLE CROPPEDIMAGES ("
            + "ID INT NOT NULL PRIMARY KEY, "
            + "FILEPATH1 VARCHAR(255) NOT NULL, "
            + "FILEPATH2 VARCHAR(255), "
            + "MD51 VARCHAR(32) NOT NULL, "
            + "MD52 VARCHAR(32), "
            + "CHARACTERNAME VARCHAR(50) NOT NULL)";
    private static final String IMAGE_TABLE_ADD_UNIQUE = "ALTER TABLE CROPPEDIMAGES ADD UNIQUE (FILEPATH1)";
    private static final String IMAGE_TABLE_ADD_UNIQUE3 = "ALTER TABLE CROPPEDIMAGES ADD UNIQUE (FILEPATH2)";
    private static final String IMAGE_TABLE_ADD_UNIQUE2 = "ALTER TABLE CROPPEDIMAGES ADD UNIQUE (MD51)";
    private static final String IMAGE_TABLE_ADD_UNIQUE4 = "ALTER TABLE CROPPEDIMAGES ADD UNIQUE (MD52)";

    private static final String CREATE_SETTINGS_TABLE = "CREATE TABLE SETTINGS ("
            + "ID INTEGER IDENTITY PRIMARY KEY, "
            + "NAME VARCHAR(255) NOT NULL, "
            + "VALUE VARCHAR(255) )";
    private static final String SETTINGS_TABLE_ADD_UNIQUE
            = "ALTER TABLE SETTINGS ADD UNIQUE (NAME,VALUE)";

    private static final String DROP_IMAGE_TABLE = "DROP TABLE CROPPEDIMAGES";

    public static void shutDown() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SHUTDOWN");
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     *
     * @param table
     * @param insertFields
     * @param insertValues
     * @return
     */
    public static int insertIntoTable(DBTable table, String[] insertFields, Object[] insertValues) {
        if (insertFields.length == 0 || insertValues.length == 0) {
            LOGGER.error("You must provide at least one field to insert a value into.");
            return INPUT_ERROR;
        }
        if (insertFields.length != insertValues.length) {
            LOGGER.error("Number of fields and values do not match.");
            return INPUT_ERROR;
        }
        String query = "INSERT INTO ".concat(table.name())
                .concat(" (");
        for (String field : insertFields) {
            query = query.concat(field)
                    .concat(",");
        }
        query = query.substring(0, query.length() - 1)
                .concat(") VALUES (");
        for (String insertField : insertFields) {
            query = query.concat("?,");
        }
        query = query.substring(0, query.length() - 1)
                .concat(")");
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            stmt = connection.prepareStatement(query);
            int i = 1;
            for (Object value : insertValues) {
                stmt.setObject(i, value);
                i++;
            }
            return stmt.executeUpdate();
        } catch (Exception e) {
            if (e.getMessage()
                    .contains("unique constraint or index violation")) {
                return DUPLICATE_ERROR;
            }
            LOGGER.error("Error inserting data into DB!", e);
            return DB_ERROR;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     *
     * @param table
     * @return
     */
    public static DBResponse selectFromTable(DBTable table) {
        return selectFromTable(table, new String[]{"*"}, new String[]{}, new Object[]{});
    }

    /**
     *
     * @param table
     * @param orderByField
     * @param orderByOperand
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String orderByField, String orderByOperand) {
        return selectFromTable(table, new String[]{"*"}, new String[]{}, new Object[]{}, "AND", orderByField, orderByOperand);
    }

    /**
     *
     * @param table
     * @param whereFields
     * @param whereValues
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String[] whereFields, Object[] whereValues) {
        return selectFromTable(table, new String[]{"*"}, whereFields, whereValues);
    }

    /**
     *
     * @param table
     * @param whereFields
     * @param whereValues
     * @param orderByField
     * @param orderByOperand
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String[] whereFields, Object[] whereValues, String orderByField, String orderByOperand) {
        return selectFromTable(table, new String[]{"*"}, whereFields, whereValues, "AND", orderByField, orderByOperand);
    }

    /**
     *
     * @param table
     * @param returnFields
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String[] returnFields) {
        return selectFromTable(table, returnFields, new String[]{}, new Object[]{});
    }

    /**
     *
     * @param table
     * @param returnFields
     * @param whereFields
     * @param whereValues
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String[] returnFields, String[] whereFields, Object[] whereValues) {
        return selectFromTable(table, returnFields, whereFields, whereValues, "AND");
    }

    /**
     *
     * @param table
     * @param returnFields
     * @param whereFields
     * @param whereValues
     * @param operand
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String[] returnFields, String[] whereFields, Object[] whereValues, String operand) {
        return selectFromTable(table, returnFields, whereFields, whereValues, operand, null, null);
    }

    // Pass a single "*" to this function in returnFields to select all fields.
    // Returns null if arguments are incorrect, DBResponse object otherwise.
    /**
     *
     * @param table
     * @param returnFields
     * @param whereFields
     * @param whereValues
     * @param operand
     * @param orderByField
     * @param orderByOperand
     * @return
     */
    public static DBResponse selectFromTable(DBTable table, String[] returnFields, String[] whereFields, Object[] whereValues, String operand,
            String orderByField, String orderByOperand) {
        if (table == null || returnFields == null || whereFields == null || whereValues == null) {
            LOGGER.error("No null arguments are accepted for this function.");
            return null;
        }
        if (returnFields.length == 0) {
            LOGGER.error("No return fields specified. If you want all fields, pass a 1-length array with \"*\" as the field.");
            return null;
        }
        if (whereFields.length != whereValues.length) {
            LOGGER.error("Length of where fields and values arrays do not match.");
            return null;
        }
        DBResponse response = new DBResponse();
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        Connection connection = null;
        String query = "SELECT ";
        if (returnFields.length == 1 && returnFields[0].equals("*")) {
            query = query.concat("* FROM ");
        } else {
            for (String field : returnFields) {
                query = query.concat(field)
                        .concat(", ");
            }
            query = query.substring(0, query.length() - 2);
            query = query.concat(" FROM ");
        }
        query = query.concat(table.name());
        if (whereFields.length > 0) {
            query = query.concat(" WHERE ");
            for (int i = 0; i < whereFields.length; i++) {
                String field = whereFields[i];
                Object value = whereValues[i];
                if (value instanceof DBSyntax) {
                    if (value.equals(DBSyntax.IS_NULL)) {
                        query = query.concat(field)
                                .concat(" IS NULL ")
                                .concat(operand)
                                .concat(" ");
                    } else if (value.equals(DBSyntax.IS_NOT_NULL)) {
                        query = query.concat(field)
                                .concat(" IS NOT NULL ")
                                .concat(operand)
                                .concat(" ");
                    } else {
                        query = query.concat(field)
                                .concat(" = ? ")
                                .concat(operand)
                                .concat(" ");
                    }
                } else {
                    query = query.concat(field)
                            .concat(" = ? ")
                            .concat(operand)
                            .concat(" ");
                }
            }
            query = query.substring(0, query.length() - 2 - operand.length());
        }
        if (orderByField != null && orderByOperand != null) {
            query = query.concat(" ORDER BY ")
                    .concat(orderByField)
                    .concat(" ")
                    .concat(orderByOperand);
        }
        try {
            connection = connectionPool.getConnection();
            preparedStmt = connection.prepareStatement(query);
            int j = 1;
            for (Object value : whereValues) {
                if (value instanceof DBSyntax) {
                    if (!(value.equals(DBSyntax.IS_NULL) || value.equals(DBSyntax.IS_NOT_NULL))) {
                        preparedStmt.setObject(j, value);
                        j++;
                    }
                } else {
                    preparedStmt.setObject(j, value);
                    j++;
                }
            }
            rs = preparedStmt.executeQuery();
            ArrayList<TreeMap<String, Object>> results = new ArrayList<>();
            ResultSetMetaData metadata = rs.getMetaData();
            int count = metadata.getColumnCount();
            while (rs.next()) {
                TreeMap<String, Object> map = new TreeMap<>();
                for (int i = 1; i <= count; i++) {
                    map.put(metadata.getColumnName(i), rs.getObject(i));
                }
                results.add(map);
            }
            response.setReturnedRows(results);
            response.setSuccessful(true);
            return response;
        } catch (Exception e) {
            LOGGER.error("Failed to select data from table!", e);
            response.setSuccessful(false);
            response.setMessage(e.getMessage());
            return response;
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(preparedStmt);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Deletes from the given table, where the given fields in whereFields are equal to the given values in whereValues. Example usage:
     * <p>
     * deleteFromTable(DBTables.IMAGETWEETS,
     * <p>
     * new String[]{"id"},
     * <p>
     * new Object[]{1234});
     * <p>
     * The above code would execute "DELETE FROM imagetweets WHERE id=1234".
     *
     * @param table The table to delete from. Enumerated to prevent an invalid table name being submitted to this method.
     * @param whereFields The fields to check against when deleting. You cannot provide an empty array.
     * @param whereValues The value conditions for the fields. This cannot be empty, and must be of equal length to the whereFields array.
     * @return The JDBC result of executing the delete query; DB_ERROR if an error occurs.
     */
    public static int deleteFromTable(DBTable table, String[] whereFields, Object[] whereValues) {
        String tableName = table.name();
        if (whereFields.length == 0 || whereValues.length == 0) {
            LOGGER.error("This function requires all arguments - you cannot call it with an empty array.");
            return INPUT_ERROR;
        }
        if (whereFields.length != whereValues.length) {
            LOGGER.error("Update and where arrays passed to this function must be of equal length.");
            return INPUT_ERROR;
        }
        String query = "DELETE FROM ".concat(tableName)
                .concat(" WHERE ");
        for (String field : whereFields) {
            query = query.concat(field)
                    .concat("=? AND ");
        }
        query = query.substring(0, query.length() - 4);
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            stmt = connection.prepareStatement(query);
            int i = 1;
            for (Object where : whereValues) {
                stmt.setObject(i, where);
                i++;
            }
            return stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Error deleting from DB!", e);
            return DB_ERROR;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     *
     * @param tableName
     * @param updateFields
     * @param updateValues
     * @param whereFields
     * @param whereValues
     * @return
     */
    public static int updateTable(DBTable tableName, String[] updateFields, Object[] updateValues, String[] whereFields, Object[] whereValues) {
        return updateTable(tableName, updateFields, updateValues, whereFields, whereValues, new String[]{}, new int[]{});
    }

    /**
     *
     * @param table
     * @param updateFields
     * @param updateValues
     * @param whereFields
     * @param whereValues
     * @param incrementFields
     * @param incrementValues
     * @return
     */
    public static int updateTable(DBTable table, String[] updateFields, Object[] updateValues, String[] whereFields, Object[] whereValues,
            String[] incrementFields, int[] incrementValues) {
        String tableName = table.name();
        if (whereFields.length == 0 || whereValues.length == 0) {
            LOGGER.error("This function requires WHERE arguments - you cannot call it with empty arrays for those.");
            return INPUT_ERROR;
        }
        if ((updateFields.length == 0 || updateValues.length == 0) && (incrementFields.length == 0 || incrementValues.length == 0)) {
            LOGGER.error("You must pass at least one field to update.");
            return INPUT_ERROR;
        }
        if ((updateFields.length != updateValues.length) || (whereFields.length != whereValues.length)
                || (incrementFields.length != incrementValues.length)) {
            LOGGER.error("Update and where arrays passed to this function must be of equal length.");
            return INPUT_ERROR;
        }
        String query = "UPDATE ".concat(tableName)
                .concat(" SET ");
        for (String f : updateFields) {
            query = query.concat(f)
                    .concat("=?, ");
        }

        for (int i = 0; i < incrementFields.length; i++) {
            String f = incrementFields[i];
            if (incrementValues[i] < 0) {
                query = query.concat(f)
                        .concat("=")
                        .concat(f)
                        .concat(String.valueOf(incrementValues[i]))
                        .concat(", ");
            } else {
                query = query.concat(f)
                        .concat("=")
                        .concat(f)
                        .concat("+")
                        .concat(String.valueOf(incrementValues[i])
                                .concat(", "));
            }
        }
        query = query.substring(0, query.length() - 2);
        query = query.concat(" WHERE ");
        for (String f : whereFields) {
            query = query.concat(f)
                    .concat("=? AND ");
        }

        query = query.substring(0, query.length() - 4);
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            stmt = connection.prepareStatement(query);
            int i = 1;
            for (Object value : updateValues) {
                stmt.setObject(i, value);
                i++;
            }
            for (Object where : whereValues) {
                stmt.setObject(i, where);
                i++;
            }
            return stmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Failed to update values in DB!", e);
            return DB_ERROR;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }

    public static int insertSetting(String name, String value) {
        DBResponse response = selectFromTable(DBTable.SETTINGS,
                new String[]{"NAME"},
                new Object[]{name});
        if (!response.isSuccessful()) {
            return DB_ERROR;
        }
        if (response.getReturnedRows()
                .size() == 1) {
            return CoreDB.updateTable(DBTable.SETTINGS,
                    new String[]{"VALUE"},
                    new Object[]{value},
                    new String[]{"NAME"},
                    new Object[]{name});
        } else {
            return CoreDB.insertIntoTable(DBTable.SETTINGS,
                    new String[]{"NAME", "VALUE"},
                    new Object[]{name, value});
        }

    }

    public static DBResponse getSettingByName(String name) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DBResponse response = new DBResponse();
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement("SELECT VALUE FROM SETTINGS WHERE NAME=?");
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                response.setReturnedObject(rs.getString("VALUE"));
            }
            response.setSuccessful(true);
            return response;
        } catch (Exception e) {
            LOGGER.error(e);
            response.setSuccessful(false);
            return response;
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    public static TreeMap<String, String> getAllSettings() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        TreeMap<String, String> settings = new TreeMap<>();
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT NAME,VALUE FROM SETTINGS");
            while (rs.next()) {
                settings.put(rs.getString("NAME"), rs.getString("VALUE"));
            }
            return settings;
        } catch (Exception e) {
            LOGGER.error(e);
            return settings;
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    public static boolean doesDBExist() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT * FROM SETTINGS LIMIT 1");
            return true;
        } catch (Exception e) {
            if (!e.getMessage()
                    .contains("object not found")) {
                LOGGER.error(e);
            }
            return false;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    public static ArrayList<String> getCharactersInDB() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT DISTINCT CHARACTERNAME FROM CROPPEDIMAGES");
            while (rs.next()) {
                names.add(rs.getString("CHARACTERNAME"));
            }
            return names;
        } catch (Exception e) {
            LOGGER.error(e);
            return names;
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    public static boolean doesTableExist() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT * FROM SETTINGS LIMIT 1");
            return true;
        } catch (Exception e) {
            if (!e.getMessage()
                    .contains("object not found")) {
                LOGGER.error(e);
            }
            return false;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    public static void initialise() {
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        connectionPool.setUsername("SA");
        connectionPool.setPassword("");
        String url = "jdbc:hsqldb:file:db/pimdb";
        connectionPool.setUrl(url);
        connectionPool.setInitialSize(10);
        connectionPool.setMaxOpenPreparedStatements(10);
        connectionPool.setMaxConnLifetimeMillis(1000 * 60 * 5);
        connectionPool.setMaxTotal(10);
    }

    public static void dropTable() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            int res = stmt.executeUpdate(DROP_IMAGE_TABLE);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    public static void createTables() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(CREATE_SETTINGS_TABLE);
            stmt = conn.createStatement();
            stmt.executeUpdate(SETTINGS_TABLE_ADD_UNIQUE);
            stmt = conn.createStatement();
            stmt.executeUpdate(CREATE_IMAGE_TABLE);
            stmt = conn.createStatement();
            stmt.executeUpdate(IMAGE_TABLE_ADD_UNIQUE);
            stmt = conn.createStatement();
            stmt.executeUpdate(IMAGE_TABLE_ADD_UNIQUE2);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }
}
