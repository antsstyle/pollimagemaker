/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class GraphicsTools {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void drawTextWithOutline(Graphics2D g, String text, int xStart, int yStart) {
        g.setColor(Color.BLACK);
        g.drawString(text, ShiftWest(xStart, 1), ShiftNorth(yStart, 1));
        g.drawString(text, ShiftWest(xStart, 1), ShiftSouth(yStart, 1));
        g.drawString(text, ShiftEast(xStart, 1), ShiftNorth(yStart, 1));
        g.drawString(text, ShiftEast(xStart, 1), ShiftSouth(yStart, 1));
        g.setColor(Color.WHITE);
        g.drawString(text, xStart, yStart);
    }

    private static int ShiftNorth(int p, int distance) {
        return (p - distance);
    }

    private static int ShiftSouth(int p, int distance) {
        return (p + distance);
    }

    private static int ShiftEast(int p, int distance) {
        return (p + distance);
    }

    private static int ShiftWest(int p, int distance) {
        return (p - distance);
    }

}
