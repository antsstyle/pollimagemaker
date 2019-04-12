/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class CropManagerListener extends MouseAdapter {

    public static JPanel cropSelectionPanel;
    public static ArrayList<String> selectedPaths = new ArrayList<>();
    private String filePath;
    private static final Logger LOGGER = LogManager.getLogger();
    private JLabel imageLabel;
    private boolean selected = false;

    public CropManagerListener(JLabel imageLabel, String filePath) {
        this.imageLabel = imageLabel;
        this.filePath = filePath;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!selected) {
            selected = true;
            imageLabel.setBorder(new LineBorder(Color.RED, 5));
            selectedPaths.add(filePath);
        } else {
            selected = false;
            imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
            selectedPaths.remove(filePath);
        }
        cropSelectionPanel.revalidate();
        cropSelectionPanel.repaint();
        cropSelectionPanel.requestFocus();
    }

}
