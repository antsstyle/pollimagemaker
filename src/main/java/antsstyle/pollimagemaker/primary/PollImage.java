/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.primary;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 * @author Ant
 */
public class PollImage {

    private BufferedImage image;
    private Graphics2D graphics;

    /**
     *
     * @param width
     * @param height
     */
    public PollImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
    }

    /**
     *
     * @param imageToDraw
     * @param xcoord
     * @param ycoord
     */
    public void drawImageOntoPollImage(BufferedImage imageToDraw, int xcoord, int ycoord) {
        graphics.drawImage(image, xcoord, ycoord, null);
    }

    /**
     * Creates a resized version of this PollImage's BufferedImage, with the given dimensions and whitespace, with the old image drawn in at the given
     * coordinates.
     *
     * @param newWidth The width of the new image.
     * @param newHeight The height of the new image.
     * @param xcoord The X coordinate from which to begin drawing the existing image into the new image.
     * @param ycoord The Y coordinate from which to begin drawing the existing image into the new image.
     */
    public void resizeImage(int newWidth, int newHeight, int xcoord, int ycoord) {
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        graphics.dispose();
        graphics = newImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, newWidth, newWidth);
        graphics.drawImage(image, xcoord, ycoord, null);
        image = newImage;
    }

    /**
     *
     * @param srcImg
     * @param paneWidth
     * @param paneHeight
     * @param scaleLevel
     * @return
     */
    public static BufferedImage getScaledImageKeepAspects(BufferedImage srcImg, int paneWidth, int paneHeight, int scaleLevel) {
        if (scaleLevel == 0) {
            return srcImg;
        }
        int width = srcImg.getWidth();
        int height = srcImg.getHeight();

        if (scaleLevel == 1 && (width <= paneWidth || height <= paneHeight)) {
            return srcImg;
        }

        if (width <= paneWidth && height <= paneHeight) {
            return srcImg;
        } else {
            double heightRatio = (double) height / paneHeight;
            double widthRatio = (double) width / paneWidth;
            int newHeight;
            int newWidth;
            if (heightRatio > widthRatio) {
                if (scaleLevel == 2) {
                    newHeight = (int) Math.round(height / heightRatio);
                    newWidth = (int) Math.round(width / heightRatio);
                } else {
                    newHeight = (int) Math.round(height / widthRatio);
                    newWidth = (int) Math.round(width / widthRatio);
                }
                return getScaledImage(srcImg, newWidth, newHeight);
            } else {
                if (scaleLevel == 2) {
                    newHeight = (int) Math.round(height / widthRatio);
                    newWidth = (int) Math.round(width / widthRatio);
                } else {
                    newHeight = (int) Math.round(height / heightRatio);
                    newWidth = (int) Math.round(width / heightRatio);
                }
                return getScaledImage(srcImg, newWidth, newHeight);
            }
        }
    }

    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     *
     * @param srcImg - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    public static BufferedImage getScaledImage(BufferedImage srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();

        HashMap<RenderingHints.Key, Object> map = new HashMap<>();
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        map.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        RenderingHints hints = new RenderingHints(map);
        g2.setRenderingHints(hints);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

}
