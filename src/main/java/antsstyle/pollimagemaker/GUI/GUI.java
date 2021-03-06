/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import antsstyle.pollimagemaker.configuration.Config;
import antsstyle.pollimagemaker.db.CoreDB;
import antsstyle.pollimagemaker.primary.PollImageMakerMain;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class GUI extends javax.swing.JFrame {

    private static final Logger LOGGER = LogManager.getLogger();
    private static GUI gui;

    private static final MainPanel MAIN_PANEL = new MainPanel();
    private static final SettingsPanel SETTINGS_PANEL = new SettingsPanel();
    private static final AboutPanel ABOUT_PANEL = new AboutPanel();

    public static MainPanel getMainPanel() {
        return MAIN_PANEL;
    }

    public static SettingsPanel getSettingsPanel() {
        return SETTINGS_PANEL;
    }
    
    public static AboutPanel getAboutPanel() {
        return ABOUT_PANEL;
    }

    public static PrimaryDisplayPanel getPrimaryDisplayPanel() {
        return PRIMARY_DISPLAY_PANEL;
    }

    public static GUI getInstance() {
        if (gui == null) {
            gui = new GUI();
        }
        return gui;
    }

    /**
     * Creates new form MainGUI
     */
    public GUI() {
        initComponents();
        setIconImage(Toolkit.getDefaultToolkit()
                .getImage(GUI.class.getResource("/isiltari64.jpg")));
    }

    /**
     * Sets the size of the main JFrame. Used to automatically resize ArtPoster according to the size of the panel being displayed.
     *
     * @param panelToDisplay The JPanel whose dimensions to adjust the JFrame's size with respect to.
     */
    public void setGUISize(JPanel panelToDisplay) {
        int xOffset = 0;
        int yOffset = 0;
        Insets insets = this.getInsets();
        xOffset += insets.left;
        xOffset += insets.right;
        yOffset += insets.top;
        yOffset += insets.bottom;
        if (PRIMARY_DISPLAY_PANEL.sideToolBar.isVisible()) {
            xOffset += PRIMARY_DISPLAY_PANEL.sideToolBar.getWidth();
        }
        Dimension d;
        Dimension dimensions = panelToDisplay.getPreferredSize();
        d = new Dimension((int) dimensions.getWidth() + xOffset, (int) dimensions.getHeight() + yOffset);
        this.setSize(d);
    }

    /**
     * Switches the display panel the GUI is displaying to the given panel, and resizes the frame in accordance with the size of that panel.
     *
     * @param panel The panel to switch to.
     */
    public void switchPanels(JPanel panel) {
        PRIMARY_DISPLAY_PANEL.getDisplayAreaPanel()
                .removeAll();
        PRIMARY_DISPLAY_PANEL.getDisplayAreaPanel()
                .add(panel);
        panel.setSize(panel.getPreferredSize());
        setGUISize(panel);
        PRIMARY_DISPLAY_PANEL.getDisplayAreaPanel()
                .revalidate();
        PRIMARY_DISPLAY_PANEL.getDisplayAreaPanel()
                .repaint();
        PRIMARY_DISPLAY_PANEL.revalidate();
        PRIMARY_DISPLAY_PANEL.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PRIMARY_DISPLAY_PANEL = new antsstyle.pollimagemaker.GUI.PrimaryDisplayPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Poll Image Maker");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PRIMARY_DISPLAY_PANEL, javax.swing.GroupLayout.DEFAULT_SIZE, 1158, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PRIMARY_DISPLAY_PANEL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Config.saveConfiguration();
        CoreDB.shutDown();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private static antsstyle.pollimagemaker.GUI.PrimaryDisplayPanel PRIMARY_DISPLAY_PANEL;
    // End of variables declaration//GEN-END:variables
}
