/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.db;

/**
 *
 * @author Ant
 */
public enum DBTable {

    CROPPEDIMAGES("CROPPEDIMAGES"),
    SETTINGS("SETTINGS");

    private String tableName;

    private DBTable(String tableName) {
        this.tableName = tableName;
    }

}
