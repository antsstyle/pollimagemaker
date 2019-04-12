/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import antsstyle.pollimagemaker.configuration.Config;
import antsstyle.pollimagemaker.tools.GraphicsTools;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class CustomLabel extends JLabel {

    private static final Logger LOGGER = LogManager.getLogger();
    private int number;
    private String text;
    private Image image1;
    private Image image2;

    public void setPollText(String text) {
        this.text = text;
    }

    public CustomLabel(Image image1, Image image2) {
        super();
        this.image1 = image1;
        this.image2 = image2;
        this.number = MainPanel.pollRows.size() + 1;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setImage1(Image image) {
        image1 = image;
    }

    public void setImage2(Image image) {
        image2 = image;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (image1 == null && image2 == null) {
            return;
        }
        if (image1 != null) {
            g2d.drawImage(image1, 0, 0, null);
        }
        if (image2 != null) {
            g2d.drawImage(image2, 300, 0, null);
        }
        g2d.setFont(new Font("Impact", Font.BOLD, 35));
        FontMetrics metrics = g2d.getFontMetrics();
        int height = metrics.getHeight();
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        int yValueNumberOffset = height;
        if (!Config.OMIT_NUMBERS) {
            GraphicsTools.drawTextWithOutline(g2d, String.valueOf(number), 10, yValueNumberOffset);
        }
        if (text != null) {
            int width = metrics.stringWidth(text);
            int fontSize = 35;
            while (width >= 600) {
                fontSize -= 5;
                g2d.setFont(new Font("Impact", Font.BOLD, fontSize));
                metrics = g2d.getFontMetrics();
                width = metrics.stringWidth(text);
            }
            int startpoint = (600 - width) / 2;
            int h = 290;
            GraphicsTools.drawTextWithOutline(g2d, text, startpoint, h);
        }
    }

}
