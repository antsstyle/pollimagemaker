/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.primary;

import antsstyle.pollimagemaker.GUI.GUI;
import antsstyle.pollimagemaker.configuration.Config;
import antsstyle.pollimagemaker.db.CoreDB;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class PollImageMakerMain {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.warn("Could not initialise UI look and feel - reverting to default look and feel.");
        }

        CoreDB.initialise();
        if (!CoreDB.doesTableExist()) {
            CoreDB.createTables();
        }
        if (!Config.initialise()) {
            LOGGER.error("Failed to initialise configuration!");
        }
        GUI.getInstance()
                .setContentPane(GUI.getPrimaryDisplayPanel());
        GUI.getInstance()
                .switchPanels(GUI.getMainPanel());
        GUI.getMainPanel().initialiseMainPanel();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            GUI.getInstance()
                    .setVisible(true);
        });
    }

}
