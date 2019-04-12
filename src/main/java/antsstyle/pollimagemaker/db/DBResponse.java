/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.db;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Container class, that wraps all the different parts of a database query result into one object.
 *
 * @author Ant
 * @since 1.0
 */
public class DBResponse {

    private boolean successful;
    private String message;
    private ArrayList<TreeMap<String, Object>> returnedRows;
    private boolean duplicateError = false;
    private Object returnedObject;

    public Object getReturnedObject() {
        return returnedObject;
    }

    public DBResponse setReturnedObject(Object returnedObject) {
        this.returnedObject = returnedObject;
        return this;
    }
    
    

    /**
     * Gets whether the error the database returned was a duplicate error or not.
     *
     * @return True if the database error was due to trying to add a duplicate record; false otherwise.
     */
    public boolean isDuplicateError() {
        return duplicateError;
    }

    /**
     * Sets whether the error in this DBResponse was due to trying to add a duplicate record or not.
     *
     * @param duplicateError The boolean value to set.
     * @return This object.
     */
    public DBResponse setDuplicateError(boolean duplicateError) {
        this.duplicateError = duplicateError;
        return this;
    }

    /**
     * Gets whether this query was successful or not.
     *
     * @return True if the DB query encountered no errors; false otherwise.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Sets whether this query was successful or not.
     *
     * @param successful The boolean value to set.
     * @return This object.
     */
    public DBResponse setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }

    /**
     * Gets the ArtPoster response message.
     *
     * @return The response message that ArtPoster set.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the ArtPoster response message.
     *
     * @param message The message to set.
     * @return This object.
     */
    public DBResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the rows returned from the database query.
     *
     * @return A list of rows returned from the database query.
     */
    public ArrayList<TreeMap<String, Object>> getReturnedRows() {
        return returnedRows;
    }

    /**
     * Sets the rows returned from the database query.
     *
     * @param returnedRows The list to set.
     * @return This object.
     */
    public DBResponse setReturnedRows(ArrayList<TreeMap<String, Object>> returnedRows) {
        this.returnedRows = returnedRows;
        return this;
    }

}
