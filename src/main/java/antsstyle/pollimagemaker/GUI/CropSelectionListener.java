/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class CropSelectionListener extends MouseAdapter {

    public static JPanel cropSelectionPanel;
    private static JLabel lastSelection;
    public static BufferedImage selectedImage;
    private static final Logger LOGGER = LogManager.getLogger();
    private JLabel imageLabel;
    private BufferedImage img;

    public CropSelectionListener(JLabel imageLabel, BufferedImage img) {
        this.imageLabel = imageLabel;
        this.img = img;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (lastSelection != null) {
            lastSelection.setBorder(new LineBorder(Color.BLACK, 1));
        }
        lastSelection = imageLabel;
        selectedImage = this.img;
        imageLabel.setBorder(new LineBorder(Color.RED, 5));
        cropSelectionPanel.revalidate();
        cropSelectionPanel.repaint();
        cropSelectionPanel.requestFocus();
    }

}
