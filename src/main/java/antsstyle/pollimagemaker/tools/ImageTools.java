/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.tools;

import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class ImageTools {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     */
    public static final List<String> IMAGE_FILE_EXTENSIONS
            = Arrays.asList("jpg", "png", "tif", "tiff", "bmp", "jpeg");

    /**
     *
     * @param imageURI
     * @return
     */
    public static boolean isImageFile(String imageURI) {
        int dotIndex = imageURI.lastIndexOf(".") + 1;
        String extension = imageURI.substring(dotIndex, imageURI.length())
                .trim()
                .toLowerCase();
        return IMAGE_FILE_EXTENSIONS.contains(extension);
    }

    public static Dimension getImageDimensionsFromFile(File imageFile) {
        try (ImageInputStream in = ImageIO.createImageInputStream(imageFile)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            } else {
                return new Dimension(-1,-1);
            }
        } catch (Exception e) {
            LOGGER.error(e);
            return new Dimension(-1,-1);
        }
    }
}
