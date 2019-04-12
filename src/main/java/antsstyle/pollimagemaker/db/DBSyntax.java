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
public enum DBSyntax {

    IS_NOT_NULL("IS NOT NULL", 1),
    IS_NULL("IS NULL", 2);

    private String name;
    private int id;

    private DBSyntax(String name, int id) {
        this.name = name;
        this.id = id;
    }

}
