/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import antsstyle.pollimagemaker.configuration.Config;
import antsstyle.pollimagemaker.configuration.DefaultConfig;
import antsstyle.pollimagemaker.datastructures.CropQueueEntry;
import antsstyle.pollimagemaker.datastructures.PollRowHolder;
import antsstyle.pollimagemaker.primary.PollImage;
import antsstyle.pollimagemaker.tools.ImageTools;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class DNDListener implements DropTargetListener {

    private static final Logger LOGGER = LogManager.getLogger();

    public DNDListener() {

    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);

        Transferable t = dtde.getTransferable();
        DataFlavor[] df = t.getTransferDataFlavors();

        for (DataFlavor f : df) {

            if (f.isFlavorJavaFileListType()) {
                List<File> files;
                try {
                    files = (List<File>) t.getTransferData(f);
                } catch (Exception e) {
                    LOGGER.error(e);
                    return;
                }
                for (File file : files) {
                    displayImage(file.getPath());
                }
            }

        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    public static void displayImage(BufferedImage img, boolean fromCrop) {
        if (img.getWidth() > 1200 || img.getHeight() > 800) {
            img = (BufferedImage) PollImage.getScaledImageKeepAspects(img, 1200, 800, 2);
        }
        double aspectRatio = (double) img.getWidth() / (double) img.getHeight();
        Rectangle capturedRect;
        boolean dualWidthImage;
        if (MainPanel.inMiddleOfPanel) {
            dualWidthImage = false;
        } else {
            dualWidthImage = !MainPanel.inMiddleOfPanel
                    && aspectRatio > Config.RECT_ASPECTR_LOWER_LIM && aspectRatio < Config.RECT_ASPECTR_HIGHER_LIM;
        }
        if (!fromCrop) {
            ImageCropDialogFrame.queue.add(new CropQueueEntry(img, fromCrop));
            ImageCropDialogFrame.begin();
        } else {
            capturedRect = null;
            processImage(img, fromCrop, capturedRect, dualWidthImage);
        }

    }

    public static void processImage(BufferedImage img, boolean fromCrop, Rectangle capturedRect, boolean dualWidthImage) {
        if (img.getWidth() > 1200 || img.getHeight() > 800) {
            img = (BufferedImage) PollImage.getScaledImageKeepAspects(img, 1200, 800, 2);
        }
        double aspectRatio = (double) img.getWidth() / (double) img.getHeight();
        if (dualWidthImage && capturedRect != null && capturedRect.width < 100 && capturedRect.height < 50) {
            int confirmAction = JOptionPane.showConfirmDialog(
                    null,
                    "You have selected an image region smaller than 100x50 pixels - are you sure you want to do this?\n"
                    + "It is likely to result in bad image quality when scaled upwards.",
                    "Low resolution image region - confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirmAction != JOptionPane.YES_OPTION) {
                return;
            }
        } else if (!dualWidthImage && capturedRect != null && capturedRect.width < 50 && capturedRect.height < 50) {
            int confirmAction = JOptionPane.showConfirmDialog(
                    null,
                    "You have selected an image region smaller than 50x50 pixels - are you sure you want to do this?\n"
                    + "It is likely to result in bad image quality when scaled upwards.",
                    "Low resolution image region - confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirmAction != JOptionPane.YES_OPTION) {
                return;
            }
        } else if (img.getWidth() < 50 || img.getHeight() < 50) {
            int confirmAction = JOptionPane.showConfirmDialog(
                    null,
                    "You have loaded an image smaller than 50x50 pixels - are you sure you want to do this?\n"
                    + "It is likely to result in bad image quality when scaled upwards.",
                    "Low resolution image - confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirmAction != JOptionPane.YES_OPTION) {
                return;
            }
        }
        BufferedImage croppedImg;
        if (capturedRect == null) {
            if (dualWidthImage && aspectRatio >= DefaultConfig.RECT_ASPECTR_LOWER_LIM && aspectRatio <= DefaultConfig.RECT_ASPECTR_HIGHER_LIM) {
                if (aspectRatio < 2) {
                    int height = (int) (img.getWidth() / 2);
                    int y = (img.getHeight() - height) / 2;
                    croppedImg = img.getSubimage(0, y, img.getWidth(), height);
                } else {
                    int width = (int) (img.getHeight() * 2);
                    int x = (img.getWidth() - width) / 2;
                    croppedImg = img.getSubimage(x, 0, width, img.getHeight());
                }
            } else if (!dualWidthImage && aspectRatio >= DefaultConfig.SQUARE_ASPECTR_LOWER_LIM && aspectRatio <= DefaultConfig.SQUARE_ASPECTR_HIGHER_LIM) {
                if (aspectRatio < 1) {
                    int height = (int) (img.getWidth());
                    int y = (img.getHeight() - height) / 2;
                    croppedImg = img.getSubimage(0, y, img.getWidth(), height);
                } else {
                    int width = (int) (img.getHeight());
                    int x = (img.getWidth() - width) / 2;
                    croppedImg = img.getSubimage(x, 0, width, img.getHeight());
                }
            } else {
                LOGGER.error("Aspect ratio: " + aspectRatio);
                LOGGER.error("Exceeded threshold.");
                JOptionPane.showMessageDialog(GUI.getInstance(), "This image has an unsupported aspect ratio to be cropped automatically."
                        + " You must select a region to add it.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            croppedImg = img.getSubimage(capturedRect.x, capturedRect.y, capturedRect.width, capturedRect.height);
        }
        if (dualWidthImage) {
            croppedImg = PollImage.getScaledImage(croppedImg, 600, 300);
        } else {
            croppedImg = PollImage.getScaledImage(croppedImg, 300, 300);
        }
        final BufferedImage croppedImageFinal = croppedImg;
        int width;
        int height;
        if (dualWidthImage) {
            width = 600;
        } else {
            width = 300;
        }
        height = 300;
        BufferedImage icon = PollImage.getScaledImageKeepAspects(croppedImg, width, height, 2);
        CustomLabel label;
        PollRowHolder holder;
        if (!MainPanel.inMiddleOfPanel) {
            label = new CustomLabel(icon, null);
            label.setSize(new Dimension(600, 300));
            label.setMaximumSize(new Dimension(600, 300));
            label.setBorder(new LineBorder(Color.BLACK, 1));
            if (!dualWidthImage) {
                MainPanel.inMiddleOfPanel = true;
            }

            GUI.getMainPanel().pollMakerPanel.add(label);
            holder = new PollRowHolder()
                    .setLabel(label)
                    .setCroppedImage1(croppedImageFinal)
                    .setDualWidthImage(dualWidthImage)
                    .setId(PollRowHolder.getNewID());
            MainPanel.pollRows.add(holder);
            GUI.getMainPanel()
                    .setPollMakerPanelSize();
        } else {
            holder = MainPanel.pollRows.get(MainPanel.pollRows.size() - 1);
            label = holder.getLabel();
            label.setImage2(icon);
            holder.setDualWidthImage(dualWidthImage);
            holder.setCroppedImage2(croppedImageFinal);
            MainPanel.inMiddleOfPanel = false;
        }
        if (!MainPanel.inMiddleOfPanel) {
            String imageText = (String) JOptionPane.showInputDialog(
                    GUI.getInstance(),
                    "Enter text for this poll option.", "");
            imageText = StringUtils.replace(imageText, " Õ ", " x ");
            holder.setPollText(imageText);
            label.setPollText(imageText);
        }
        MainPanel.refreshTextListModel();
        if (label.getMouseListeners().length == 0) {
            label.addMouseListener(new PanelListener(holder, label));
        }
        GUI.getMainPanel()
                .refreshPollMakerComponents();
    }

    private static void displayImage(final String path) {
        if (!ImageTools.isImageFile(path)) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Invalid image file.\nFiletypes supported:"
                    + "jpg, jpeg, png, tif, tiff, bmp", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        File file = new File(path);
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (Exception e) {
            LOGGER.error(e);
            return;
        }
        double aspectRatio = (double) img.getWidth() / (double) img.getHeight();
        if (((aspectRatio >= Config.SQUARE_ASPECTR_LOWER_LIM && aspectRatio <= Config.SQUARE_ASPECTR_HIGHER_LIM)
                || (aspectRatio >= Config.RECT_ASPECTR_LOWER_LIM && aspectRatio <= Config.RECT_ASPECTR_HIGHER_LIM && !MainPanel.inMiddleOfPanel))
                && Config.AUTO_CROP) {
            displayImage(img, true);
        } else {
            displayImage(img, false);
        }
    }

}
