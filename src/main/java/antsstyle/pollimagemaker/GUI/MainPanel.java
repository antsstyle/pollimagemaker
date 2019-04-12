/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import antsstyle.pollimagemaker.configuration.Config;
import antsstyle.pollimagemaker.datastructures.PollRowHolder;
import antsstyle.pollimagemaker.tools.FileTools;
import antsstyle.pollimagemaker.tools.GraphicsTools;
import antsstyle.pollimagemaker.tools.ImageTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class MainPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean inMiddleOfPanel = false;
    public static ArrayList<PollRowHolder> pollRows = new ArrayList<>();
    public static DefaultListModel textListModel = new DefaultListModel();

    /**
     * Creates new form MainPanel
     */
    public MainPanel() {
        initComponents();
    }

    public void initialiseMainPanel() {
        omitNumbersToggleButton.setSelected(Config.OMIT_NUMBERS);
    }
    
    /**
     * Updates and corrects the numbering of panels in the GUI.
     */
    public static void updatePollRowPanels() {
        for (int i = 0; i < pollRows.size(); i++) {
            pollRows.get(i)
                    .getLabel()
                    .setNumber(i + 1);
        }
    }

    public static void refreshTextListModel() {
        textListModel.clear();
        int i = 1;
        for (PollRowHolder holder : pollRows) {
            if (holder.getPollText() != null) {
                String textToAdd = String.valueOf(i)
                        .concat(". ")
                        .concat(holder.getPollText());
                textListModel.addElement(textToAdd);
                i++;
            }
        }
    }

    public void setPollMakerPanelSize() {
        int y = Math.max(600, pollRows.size() * 300);
        int x = pollMakerScrollPane.getVerticalScrollBar()
                .getWidth() + 602;
        pollMakerPanel.setSize(x, y);
        pollMakerScrollPane.setSize(x, 600);
        pollMakerPanel.setPreferredSize(new Dimension(x, y));
    }

    private boolean saveImage() {
        if (inMiddleOfPanel) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "You must finish the current poll option before saving.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (pollRows.isEmpty()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "You must add at least one poll row before saving.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private File saveCroppedImages() {
        File croppedImageDir = null;
        if (Config.SAVE_CROPPED_IMAGES) {
            try {
                croppedImageDir = new File(System.getProperty("user.dir")
                        .concat("/croppedimages"));
                if (!croppedImageDir.exists()) {
                    if (!croppedImageDir.mkdir()) {
                        LOGGER.error("Failed to create new directory to save cropped images in.");
                        croppedImageDir = null;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
        return croppedImageDir;
    }

    private void saveSubImage() {
        if (!saveImage()) {
            return;
        }
        int totalRows = pollRows.size();
        File file = presentSaveDialog();
        if (file == null) {
            return;
        }
        // Image numbers as indexes
        ArrayList<Integer> imageNumbers = new ArrayList<>();
        boolean preserveNumbers;
        JPanel savePanel = new JPanel(new BorderLayout(0, 20));
        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 2));
        checkBoxPanel.setBorder(new LineBorder(Color.BLACK, 1));
        ArrayList<JCheckBox> checkboxes = new ArrayList<>();
        for (int i = 0; i < totalRows; i++) {
            JCheckBox box = new JCheckBox();
            box.setSelected(false);
            box.setText(String.valueOf(i + 1).concat(". ").concat(pollRows.get(i).getPollText()));
            checkboxes.add(box);
            checkBoxPanel.add(box);
        }
        savePanel.add(checkBoxPanel, BorderLayout.CENTER);
        JCheckBox preserveNumbering = new JCheckBox();
        preserveNumbering.setSelected(true);
        preserveNumbering.setText("Preserve original image numbers");
        preserveNumbering.setFont(new Font("Tahoma", Font.BOLD, 14));
        savePanel.add(preserveNumbering, BorderLayout.SOUTH);
        int res = JOptionPane.showConfirmDialog(GUI.getInstance(), savePanel,
                "Please select the images you want to use.", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            preserveNumbers = preserveNumbering.isSelected();
            totalRows = 0;
            for (int i = 0; i < checkboxes.size(); i++) {
                JCheckBox box = checkboxes.get(i);
                if (box.isSelected()) {
                    totalRows++;
                    imageNumbers.add(i);
                }
            }
            if (totalRows == 0) {
                JOptionPane.showMessageDialog(GUI.getInstance(), "You must select at least one poll option to save a subimage.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Config.SAVE_DIRECTORY = file.getParent();
            file = getFileWithExtension(file);
        } else {
            return;
        }
        int borderThickness = Config.BORDER_THICKNESS;
        int totalWidth = 600 + (borderThickness * 3);
        int totalHeight = 0;
        for (int i = 0; i < pollRows.size(); i++) {
            if (!imageNumbers.contains(i)) {
                continue;
            }
            PollRowHolder holder = pollRows.get(i);
            BufferedImage img;
            img = holder.getCroppedImage1();
            totalHeight += img.getWidth() / 2;
            if (!holder.isDualWidthImage()) {
                img = holder.getCroppedImage2();
                totalHeight += img.getWidth() / 2;
            }
        }
        int perImageHeight = 300 + borderThickness;
        totalHeight += (totalRows + 1) * borderThickness;
        BufferedImage resultImg = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultImg.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, resultImg.getWidth(), resultImg.getHeight());
        g.setColor(Color.WHITE);
        int fontSize = 35;
        g.setFont(new Font("Impact", Font.BOLD, fontSize));
        FontMetrics metrics;
        int xCounter = borderThickness;
        int yCounter = borderThickness;
        int rowCounter = 1;
        boolean errorDuringCropSave = false;
        ArrayList<BufferedImage> imageList = new ArrayList<>();
        ArrayList<String> imageTexts = new ArrayList<>();
        File croppedImageDir = saveCroppedImages();
        for (int i = 0; i < pollRows.size(); i++) {
            if (!imageNumbers.contains(i)) {
                continue;
            }
            PollRowHolder holder = pollRows.get(i);
            imageList.add(holder.getCroppedImage1());
            imageTexts.add(holder.getPollText());
            if (!holder.isDualWidthImage()) {
                imageList.add(holder.getCroppedImage2());
            }
        }
        for (BufferedImage img : imageList) {
            String text = imageTexts.get(rowCounter - 1);
            text = StringUtils.replace(text, " Õ ", " x ");
            if (Config.SAVE_CROPPED_IMAGES && croppedImageDir != null) {
                try {
                    text = text.replaceAll("[:\\\\/*?|<>]\"", "_");
                    int index = text.indexOf(" x ");
                    String saveName;
                    if (index == -1) {
                        saveName = text;
                    } else if (xCounter == borderThickness) {
                        saveName = text.substring(0, index);
                    } else {
                        saveName = text.substring(index + 3, text.length());
                    }
                    saveName = saveName.concat(".png");
                    File cropSaveFile = new File(croppedImageDir.getAbsolutePath()
                            .concat("/")
                            .concat(saveName));
                    if (cropSaveFile.exists()) {
                        File cropTestMD5File = new File(croppedImageDir.getAbsolutePath()
                                .concat("/")
                                .concat("md5testimage.png"));
                        ImageIO.write(img, "png", cropTestMD5File);
                        cropTestMD5File = new File(croppedImageDir.getAbsolutePath()
                                .concat("/")
                                .concat("md5testimage.png"));
                        String originalMD5 = FileTools.getFileMD5(cropSaveFile.getAbsolutePath());
                        String newMD5 = FileTools.getFileMD5(cropTestMD5File.getAbsolutePath());
                        int i = 2;
                        int errorCounter = 50;
                        String originalContFile = cropSaveFile.getAbsolutePath()
                                .substring(0, cropSaveFile.getAbsolutePath()
                                        .length() - 4);
                        originalContFile = originalContFile.concat(String.valueOf(i))
                                .concat(".png");
                        File ocFile = new File(originalContFile);
                        boolean md5match = originalMD5.equals(newMD5);
                        while (ocFile.exists()) {
                            if (i >= errorCounter) {
                                break;
                            }
                            originalMD5 = FileTools.getFileMD5(originalContFile);
                            if (originalMD5.equals(newMD5)) {
                                md5match = true;
                                break;
                            }
                            originalContFile = originalContFile.substring(0, originalContFile.length() - 5);
                            i++;
                            originalContFile = originalContFile.concat(String.valueOf(i))
                                    .concat(".png");
                            ocFile = new File(originalContFile);
                        }
                        Files.delete(Paths.get(cropTestMD5File.getAbsolutePath()));
                        if (!md5match) {
                            i = 2;
                            String newSavePath = cropSaveFile.getAbsolutePath()
                                    .substring(0, cropSaveFile.getAbsolutePath()
                                            .length() - 4);
                            newSavePath = newSavePath.concat(String.valueOf(i))
                                    .concat(".png");
                            File f = new File(newSavePath);
                            while (f.exists()) {
                                if (i >= errorCounter) {
                                    break;
                                }
                                newSavePath = newSavePath.substring(0, newSavePath.length() - 5);
                                i++;
                                newSavePath = newSavePath.concat(String.valueOf(i))
                                        .concat(".png");
                                f = new File(newSavePath);
                            }
                            if (i != errorCounter) {
                                ImageIO.write(img, "png", f);
                            }
                        } else {
                            // Do nothing - the existing file is the same image
                        }
                    } else {
                        ImageIO.write(img, "png", cropSaveFile);
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                    errorDuringCropSave = true;
                }
            }
            g.drawImage(img, xCounter, yCounter, null);
            String numberToDraw;
            if (preserveNumbers) {
                numberToDraw = String.valueOf(imageNumbers.get(rowCounter - 1) + 1);
            } else {
                numberToDraw = String.valueOf(rowCounter);
            }
            if (img.getWidth() >= 500) {
                metrics = g.getFontMetrics();
                int width = metrics.stringWidth(text);
                while (width >= totalWidth) {
                    fontSize -= 2;
                    g.setFont(new Font("Impact", Font.BOLD, fontSize));
                    metrics = g.getFontMetrics();
                    width = metrics.stringWidth(text);
                }
                int startpoint = (totalWidth - width) / 2;
                g.setColor(Color.BLACK);
                GraphicsTools.drawTextWithOutline(g, text, startpoint, yCounter + (perImageHeight - 10));
                g.setFont(new Font("Impact", Font.BOLD, 35));
                metrics = g.getFontMetrics();
                if (!Config.OMIT_NUMBERS) {
                    GraphicsTools.drawTextWithOutline(g, numberToDraw, (10 + borderThickness), yCounter + metrics.getHeight());
                }
                rowCounter++;
                xCounter = borderThickness;
                yCounter += (img.getHeight() + borderThickness);
            } else if (xCounter == borderThickness) {
                xCounter += (img.getWidth() + borderThickness);
            } else {
                metrics = g.getFontMetrics();
                int width = metrics.stringWidth(text);
                while (width >= totalWidth) {
                    fontSize -= 2;
                    g.setFont(new Font("Impact", Font.BOLD, fontSize));
                    metrics = g.getFontMetrics();
                    width = metrics.stringWidth(text);
                }
                int startpoint = (totalWidth - width) / 2;
                GraphicsTools.drawTextWithOutline(g, text, startpoint, yCounter + (perImageHeight - 10));
                g.setFont(new Font("Impact", Font.BOLD, 35));
                metrics = g.getFontMetrics();
                if (!Config.OMIT_NUMBERS) {
                    GraphicsTools.drawTextWithOutline(g, numberToDraw, (10 + borderThickness), yCounter + metrics.getHeight());
                }
                rowCounter++;
                xCounter = borderThickness;
                yCounter += (img.getHeight() + borderThickness);
            }
        }
        g.dispose();
        try {
            String extension = FileTools.getExtensionWithoutDot(file.getAbsolutePath());
            ImageIO.write(resultImg, extension, file);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        if ((Config.SAVE_CROPPED_IMAGES && croppedImageDir != null && !errorDuringCropSave) || !Config.SAVE_CROPPED_IMAGES) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Poll image saved successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Poll image saved successfully!"
                    + " An error occurred while saving the cropped\nimages separately for later use. Consult the logs for more information.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private File presentSaveDialog() {
        JFileChooser fc = new JFileChooser();
        String dirToChooseFrom = Config.SAVE_DIRECTORY;
        if (dirToChooseFrom.equals("")) {
            dirToChooseFrom = System.getProperty("user.dir");
        }
        File testDirectory = new File(dirToChooseFrom);
        if (!testDirectory.exists() || !testDirectory.isDirectory()) {
            dirToChooseFrom = Config.SAVE_DIRECTORY;
        }
        File selectedFile = new File(dirToChooseFrom
                .concat("/pollimage.jpg"));
        fc.setSelectedFile(selectedFile);
        FileFilter imageFilter = new FileNameExtensionFilter(
                "Image files", "jpg");
        fc.addChoosableFileFilter(imageFilter);
        int result = fc.showSaveDialog(GUI.getInstance());
        if (result == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }

    private File getFileWithExtension(File file) {
        String filep = file.getAbsolutePath();
        int lastSlash = filep.lastIndexOf("\\");
        int lastDot = filep.lastIndexOf(".");
        if (lastDot < lastSlash) {
            // no ext
            filep = filep.concat(".png");
            file = new File(filep);
        } else {
            String ext = filep.substring(lastDot, filep.length());
            if (!ext.equals(".png")) {
                if (!ext.equals(".jpg")) {
                    file = new File(filep.substring(0, lastDot)
                            .concat(".png"));
                }
            }
        }
        return file;
    }

    private void saveFinishedPollImage(boolean saveSubImage) {
        if (!saveImage()) {
            return;
        }
        int totalRows = pollRows.size();
        File file = presentSaveDialog();
        if (file == null) {
            return;
        }
        // Image numbers as indexes
        ArrayList<Integer> imageNumbers = new ArrayList<>();
        boolean preserveNumbers = false;
        if (saveSubImage) {
            JPanel savePanel = new JPanel(new BorderLayout(0, 20));
            JPanel checkBoxPanel = new JPanel(new GridLayout(0, 2));
            checkBoxPanel.setBorder(new LineBorder(Color.BLACK, 1));
            ArrayList<JCheckBox> checkboxes = new ArrayList<>();
            for (int i = 0; i < totalRows; i++) {
                JCheckBox box = new JCheckBox();
                box.setSelected(false);
                box.setText(String.valueOf(i + 1).concat(". ").concat(pollRows.get(i).getPollText()));
                checkboxes.add(box);
                checkBoxPanel.add(box);
            }
            savePanel.add(checkBoxPanel, BorderLayout.CENTER);
            JCheckBox preserveNumbering = new JCheckBox();
            preserveNumbering.setSelected(true);
            preserveNumbering.setText("Preserve original image numbers");
            preserveNumbering.setFont(new Font("Tahoma", Font.BOLD, 14));
            savePanel.add(preserveNumbering, BorderLayout.SOUTH);
            int res = JOptionPane.showConfirmDialog(GUI.getInstance(), savePanel,
                    "Please select the images you want to use.", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                preserveNumbers = preserveNumbering.isSelected();
                totalRows = 0;
                for (int i = 0; i < checkboxes.size(); i++) {
                    JCheckBox box = checkboxes.get(i);
                    if (box.isSelected()) {
                        totalRows++;
                        imageNumbers.add(i);
                    }
                }
                if (totalRows == 0) {
                    JOptionPane.showMessageDialog(GUI.getInstance(), "You must select at least one poll option to save a subimage.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Config.SAVE_DIRECTORY = file.getParent();
                file = getFileWithExtension(file);
            } else {
                return;
            }
        } else {
            for (int i = 0; i < pollRows.size(); i++) {
                imageNumbers.add(i);
            }
            Config.SAVE_DIRECTORY = file.getParent();
            file = getFileWithExtension(file);
        }
        int borderThickness = Config.BORDER_THICKNESS;
        int totalWidth = 600 + (borderThickness * 3);
        int totalHeight = 0;
        for (int i = 0; i < pollRows.size(); i++) {
            if (!imageNumbers.contains(i)) {
                continue;
            }
            PollRowHolder holder = pollRows.get(i);
            BufferedImage img;
            img = holder.getCroppedImage1();
            totalHeight += img.getWidth() / 2;
            if (!holder.isDualWidthImage()) {
                img = holder.getCroppedImage2();
                totalHeight += img.getWidth() / 2;
            }
        }
        int perImageHeight = 300 + borderThickness;
        totalHeight += (totalRows + 1) * borderThickness;
        BufferedImage resultImg = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultImg.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, resultImg.getWidth(), resultImg.getHeight());
        g.setColor(Color.WHITE);
        int fontSize = 35;
        g.setFont(new Font("Impact", Font.BOLD, fontSize));
        FontMetrics metrics;
        int xCounter = borderThickness;
        int yCounter = borderThickness;
        int rowCounter = 1;
        boolean errorDuringCropSave = false;
        ArrayList<BufferedImage> imageList = new ArrayList<>();
        ArrayList<String> imageTexts = new ArrayList<>();
        File croppedImageDir = saveCroppedImages();
        for (int i = 0; i < pollRows.size(); i++) {
            if (!imageNumbers.contains(i)) {
                continue;
            }
            PollRowHolder holder = pollRows.get(i);
            imageList.add(holder.getCroppedImage1());
            imageTexts.add(holder.getPollText());
            if (!holder.isDualWidthImage()) {
                imageList.add(holder.getCroppedImage2());
            }
        }
        for (BufferedImage img : imageList) {
            String text = imageTexts.get(rowCounter - 1);
            text = StringUtils.replace(text, " Õ ", " x ");
            if (Config.SAVE_CROPPED_IMAGES && croppedImageDir != null) {
                try {
                    text = text.replaceAll("[:\\\\/*?|<>]\"", "_");
                    int index = text.indexOf(" x ");
                    String saveName;
                    if (index == -1) {
                        saveName = text;
                    } else if (xCounter == borderThickness) {
                        saveName = text.substring(0, index);
                    } else {
                        saveName = text.substring(index + 3, text.length());
                    }
                    saveName = saveName.concat(".png");
                    File cropSaveFile = new File(croppedImageDir.getAbsolutePath()
                            .concat("/")
                            .concat(saveName));
                    if (cropSaveFile.exists()) {
                        File cropTestMD5File = new File(croppedImageDir.getAbsolutePath()
                                .concat("/")
                                .concat("md5testimage.png"));
                        ImageIO.write(img, "png", cropTestMD5File);
                        cropTestMD5File = new File(croppedImageDir.getAbsolutePath()
                                .concat("/")
                                .concat("md5testimage.png"));
                        String originalMD5 = FileTools.getFileMD5(cropSaveFile.getAbsolutePath());
                        String newMD5 = FileTools.getFileMD5(cropTestMD5File.getAbsolutePath());
                        int i = 2;
                        int errorCounter = 50;
                        String originalContFile = cropSaveFile.getAbsolutePath()
                                .substring(0, cropSaveFile.getAbsolutePath()
                                        .length() - 4);
                        originalContFile = originalContFile.concat(String.valueOf(i))
                                .concat(".png");
                        File ocFile = new File(originalContFile);
                        boolean md5match = originalMD5.equals(newMD5);
                        while (ocFile.exists()) {
                            if (i >= errorCounter) {
                                break;
                            }
                            originalMD5 = FileTools.getFileMD5(originalContFile);
                            if (originalMD5.equals(newMD5)) {
                                md5match = true;
                                break;
                            }
                            originalContFile = originalContFile.substring(0, originalContFile.length() - 5);
                            i++;
                            originalContFile = originalContFile.concat(String.valueOf(i))
                                    .concat(".png");
                            ocFile = new File(originalContFile);
                        }
                        Files.delete(Paths.get(cropTestMD5File.getAbsolutePath()));
                        if (!md5match) {
                            i = 2;
                            String newSavePath = cropSaveFile.getAbsolutePath()
                                    .substring(0, cropSaveFile.getAbsolutePath()
                                            .length() - 4);
                            newSavePath = newSavePath.concat(String.valueOf(i))
                                    .concat(".png");
                            File f = new File(newSavePath);
                            while (f.exists()) {
                                if (i >= errorCounter) {
                                    break;
                                }
                                newSavePath = newSavePath.substring(0, newSavePath.length() - 5);
                                i++;
                                newSavePath = newSavePath.concat(String.valueOf(i))
                                        .concat(".png");
                                f = new File(newSavePath);
                            }
                            if (i != errorCounter) {
                                ImageIO.write(img, "png", f);
                            }
                        } else {
                            // Do nothing - the existing file is the same image
                        }
                    } else {
                        ImageIO.write(img, "png", cropSaveFile);
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                    errorDuringCropSave = true;
                }
            }
            g.drawImage(img, xCounter, yCounter, null);
            String numberToDraw;
            if (preserveNumbers) {
                numberToDraw = String.valueOf(imageNumbers.get(rowCounter - 1) + 1);
            } else {
                numberToDraw = String.valueOf(rowCounter);
            }
            if (img.getWidth() >= 500) {
                metrics = g.getFontMetrics();
                int width = metrics.stringWidth(text);
                while (width >= totalWidth) {
                    fontSize -= 2;
                    g.setFont(new Font("Impact", Font.BOLD, fontSize));
                    metrics = g.getFontMetrics();
                    width = metrics.stringWidth(text);
                }
                int startpoint = (totalWidth - width) / 2;
                g.setColor(Color.BLACK);
                GraphicsTools.drawTextWithOutline(g, text, startpoint, yCounter + (perImageHeight - 10));
                g.setFont(new Font("Impact", Font.BOLD, 35));
                metrics = g.getFontMetrics();
                if (!Config.OMIT_NUMBERS) {
                    GraphicsTools.drawTextWithOutline(g, numberToDraw, (10 + borderThickness), yCounter + metrics.getHeight());
                }
                rowCounter++;
                xCounter = borderThickness;
                yCounter += (img.getHeight() + borderThickness);
            } else if (xCounter == borderThickness) {
                xCounter += (img.getWidth() + borderThickness);
            } else {
                metrics = g.getFontMetrics();
                int width = metrics.stringWidth(text);
                while (width >= totalWidth) {
                    fontSize -= 2;
                    g.setFont(new Font("Impact", Font.BOLD, fontSize));
                    metrics = g.getFontMetrics();
                    width = metrics.stringWidth(text);
                }
                int startpoint = (totalWidth - width) / 2;
                GraphicsTools.drawTextWithOutline(g, text, startpoint, yCounter + (perImageHeight - 10));
                g.setFont(new Font("Impact", Font.BOLD, 35));
                metrics = g.getFontMetrics();
                if (!Config.OMIT_NUMBERS) {
                    GraphicsTools.drawTextWithOutline(g, numberToDraw, (10 + borderThickness), yCounter + metrics.getHeight());
                }
                rowCounter++;
                xCounter = borderThickness;
                yCounter += (img.getHeight() + borderThickness);
            }
        }
        g.dispose();
        try {
            String extension = FileTools.getExtensionWithoutDot(file.getAbsolutePath());
            ImageIO.write(resultImg, extension, file);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        if ((Config.SAVE_CROPPED_IMAGES && croppedImageDir != null && !errorDuringCropSave) || !Config.SAVE_CROPPED_IMAGES) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Poll image saved successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Poll image saved successfully!"
                    + " An error occurred while saving the cropped\nimages separately for later use. Consult the logs for more information.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static int ShiftNorth(int p, int distance) {
        return (p - distance);
    }

    public static int ShiftSouth(int p, int distance) {
        return (p + distance);
    }

    public static int ShiftEast(int p, int distance) {
        return (p + distance);
    }

    public static int ShiftWest(int p, int distance) {
        return (p - distance);
    }

    private void saveProgress() {
        if (pollRows == null || pollRows.isEmpty()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "You must add at least one image before saving your progress.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Calendar cal = Calendar.getInstance();
        Date d = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String folderName = sdf.format(d);
        folderName = "PollImage Save - ".concat(folderName);
        File f = new File(folderName);
        LOGGER.info("Saving progress to folder: " + f.getAbsolutePath());
        if (!f.exists()) {
            f.mkdir();
        }
        String baseFolderPath = f.getAbsolutePath();
        int errorCount = 0;
        String errorMessage = "";
        for (PollRowHolder holder : pollRows) {
            int id = holder.getId();
            String text = holder.getPollText();
            if (text == null) {
                text = "";
            }
            String newFolderPath = baseFolderPath.concat("/".concat(String.valueOf(id)));
            File f1 = new File(newFolderPath);
            if (!f1.exists()) {
                f1.mkdir();
            }
            String pollTextPath = f1.getAbsolutePath().concat("/polltext.txt");
            try (PrintWriter pw = new PrintWriter(pollTextPath)) {
                pw.println(text);
            } catch (Exception e) {
                LOGGER.error("Failed to save poll text file to path: " + e);
                errorCount++;
            }
            String firstImageFilePath = f1.getAbsolutePath().concat("/1.png");
            try {
                File f2 = new File(firstImageFilePath);
                ImageIO.write(holder.getCroppedImage1(), "png", f2);
            } catch (Exception e) {
                LOGGER.error("Failed to write first image file to path: " + firstImageFilePath, e);
                errorCount++;
            }
            String secondImageFilePath = f1.getAbsolutePath().concat("/2.png");
            if (!holder.isDualWidthImage()) {
                try {
                    File f3 = new File(secondImageFilePath);
                    if (holder.getCroppedImage2() != null) {
                        ImageIO.write(holder.getCroppedImage2(), "png", f3);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to write second image file to path: " + secondImageFilePath, e);
                    errorCount++;
                }
            }
        }
        if (errorCount == 0) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Progress saved successfully.", "Success!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            String errorMsg = "<html>" + String.valueOf(errorCount) + " errors occurred while saving progress. <br/><br/>Tell Ant he's an idiot, "
                    + "and/or check the logs for more information.</html>";
            JOptionPane.showMessageDialog(GUI.getInstance(), errorMsg, 
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pollMakerScrollPane = new javax.swing.JScrollPane();
        pollMakerPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        loadProgressButton = new javax.swing.JButton();
        saveProgressButton = new javax.swing.JButton();
        saveImage = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        loadCropButton = new javax.swing.JButton();
        saveSubImage = new javax.swing.JButton();
        clearImage = new javax.swing.JButton();
        omitNumbersToggleButton = new javax.swing.JToggleButton();

        setMaximumSize(new java.awt.Dimension(811, 659));
        setMinimumSize(new java.awt.Dimension(811, 659));
        setPreferredSize(new java.awt.Dimension(811, 659));

        pollMakerScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pollMakerScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pollMakerScrollPane.setMaximumSize(new java.awt.Dimension(640, 600));
        pollMakerScrollPane.setMinimumSize(new java.awt.Dimension(640, 600));
        pollMakerScrollPane.setPreferredSize(new java.awt.Dimension(640, 600));

        pollMakerPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        pollMakerPanel.setMaximumSize(new java.awt.Dimension(617, 50000));
        pollMakerPanel.setMinimumSize(new java.awt.Dimension(617, 600));
        pollMakerPanel.setPreferredSize(new java.awt.Dimension(617, 600));
        pollMakerPanel.setLayout(new javax.swing.BoxLayout(pollMakerPanel, javax.swing.BoxLayout.Y_AXIS));
        pollMakerScrollPane.setViewportView(pollMakerPanel);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Drag and drop images into the panel below to create a poll image.");

        loadProgressButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        loadProgressButton.setText("Load Progress");
        loadProgressButton.setMaximumSize(new java.awt.Dimension(145, 33));
        loadProgressButton.setMinimumSize(new java.awt.Dimension(145, 33));
        loadProgressButton.setPreferredSize(new java.awt.Dimension(145, 33));
        loadProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProgressButtonActionPerformed(evt);
            }
        });

        saveProgressButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        saveProgressButton.setText("Save Progress");
        saveProgressButton.setMaximumSize(new java.awt.Dimension(145, 33));
        saveProgressButton.setMinimumSize(new java.awt.Dimension(145, 33));
        saveProgressButton.setPreferredSize(new java.awt.Dimension(145, 33));
        saveProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProgressButtonActionPerformed(evt);
            }
        });

        saveImage.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        saveImage.setText("Save Image");
        saveImage.setMaximumSize(new java.awt.Dimension(145, 33));
        saveImage.setMinimumSize(new java.awt.Dimension(145, 33));
        saveImage.setPreferredSize(new java.awt.Dimension(145, 33));
        saveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveImageActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Poll Image Options");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Saved Crops");

        loadCropButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        loadCropButton.setText("Load Crop");
        loadCropButton.setMaximumSize(new java.awt.Dimension(145, 33));
        loadCropButton.setMinimumSize(new java.awt.Dimension(145, 33));
        loadCropButton.setPreferredSize(new java.awt.Dimension(145, 33));
        loadCropButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCropButtonActionPerformed(evt);
            }
        });

        saveSubImage.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        saveSubImage.setText("Save Sub-Image");
        saveSubImage.setMaximumSize(new java.awt.Dimension(145, 33));
        saveSubImage.setMinimumSize(new java.awt.Dimension(145, 33));
        saveSubImage.setPreferredSize(new java.awt.Dimension(145, 33));
        saveSubImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSubImageActionPerformed(evt);
            }
        });

        clearImage.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        clearImage.setForeground(new java.awt.Color(255, 0, 0));
        clearImage.setText("Clear Poll Image");
        clearImage.setMaximumSize(new java.awt.Dimension(145, 33));
        clearImage.setMinimumSize(new java.awt.Dimension(145, 33));
        clearImage.setPreferredSize(new java.awt.Dimension(145, 33));
        clearImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearImageActionPerformed(evt);
            }
        });

        omitNumbersToggleButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        omitNumbersToggleButton.setText("Omit Numbers");
        omitNumbersToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                omitNumbersToggleButtonItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(loadCropButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(omitNumbersToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(loadProgressButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(saveProgressButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(saveImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(saveSubImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clearImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pollMakerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 617, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pollMakerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveSubImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(omitNumbersToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadCropButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void saveProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProgressButtonActionPerformed
        saveProgress();
    }//GEN-LAST:event_saveProgressButtonActionPerformed

    private void saveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveImageActionPerformed
        saveFinishedPollImage(false);
    }//GEN-LAST:event_saveImageActionPerformed

    private void loadProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadProgressButtonActionPerformed
        int res;
        if (!(pollRows.isEmpty() && !inMiddleOfPanel)) {
            res = JOptionPane.showConfirmDialog(GUI.getInstance(), "Loading progress will wipe the current poll - is that OK?", "Confirm load",
                    JOptionPane.OK_CANCEL_OPTION);
        } else {
            res = JOptionPane.OK_OPTION;
        }
        if (res == JOptionPane.OK_OPTION) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int result = fc.showOpenDialog(GUI.getInstance());
            File path;
            if (result == JFileChooser.APPROVE_OPTION) {
                path = fc.getSelectedFile();
                while (!path.getName()
                        .startsWith("PollImage Save")) {
                    JOptionPane.showMessageDialog(GUI.getInstance(), "Invalid directory. Select a PollImage Save directory.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    result = fc.showOpenDialog(GUI.getInstance());
                    if (result == JFileChooser.CANCEL_OPTION) {
                        return;
                    }
                }
            } else {
                return;
            }
            for (PollRowHolder holder : pollRows) {
                pollMakerPanel.remove(holder.getLabel());
            }
            pollRows = new ArrayList<>();
            refreshPollMakerComponents();
            File[] pathDirs = path.listFiles();
            TreeMap<Integer, File> treeset = new TreeMap<>();
            try {
                for (File f : pathDirs) {
                    treeset.put(Integer.parseInt(f.getName()), f);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(GUI.getInstance(), "Your PollImage save contains invalid data. Try another.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                LOGGER.error(e);
                return;
            }
            Set<Integer> keyset = treeset.keySet();
            for (int i = 0; i < keyset.size(); i++) {
                if (treeset.get(i) == null) {
                    JOptionPane.showMessageDialog(GUI.getInstance(), "Your PollImage save contains invalid data. Try another.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            boolean error = false;
            for (int i = 0; i < keyset.size(); i++) {
                File pathDir = treeset.get(i);
                if (!pathDir.isDirectory()) {
                    continue;
                }
                PollRowHolder holder = new PollRowHolder();
                holder.setId(i);
                CustomLabel label = new CustomLabel(null, null);
                try {
                    String t = new String(Files.readAllBytes(Paths.get(pathDir.getAbsolutePath()
                            .concat("/polltext.txt")))).trim();
                    t = StringUtils.replace(t, " Õ ", " x ");
                    holder.setPollText(t);
                    label.setPollText(t);
                } catch (Exception e) {
                    LOGGER.error(e);
                    error = true;
                }
                try {
                    holder.setCroppedImage1(ImageIO.read(new File(pathDir.getAbsolutePath()
                            .concat("/1.png"))));
                } catch (Exception e) {
                    LOGGER.error(e);
                    error = true;
                }
                File secondImgFile = new File(pathDir.getAbsolutePath()
                        .concat("/2.png"));
                boolean secondFileExists = false;
                if (secondImgFile.exists()) {
                    try {
                        secondFileExists = true;
                        holder.setDualWidthImage(false);
                        holder.setCroppedImage2(ImageIO.read(new File(pathDir.getAbsolutePath()
                                .concat("/2.png"))));
                    } catch (Exception e) {
                        LOGGER.error(e);
                        error = true;
                    }
                } else if (holder.getCroppedImage1()
                        .getWidth() == 300) {
                    holder.setDualWidthImage(false);
                    secondFileExists = false;
                    inMiddleOfPanel = true;
                } else {
                    holder.setDualWidthImage(true);
                    secondFileExists = false;
                    inMiddleOfPanel = false;
                }
                label.setSize(new Dimension(600, 300));
                label.setMaximumSize(new Dimension(600, 300));
                label.setLayout(new BorderLayout());
                label.setBorder(new LineBorder(Color.BLACK, 1));
                label.setImage1(holder.getCroppedImage1());
                if (!holder.isDualWidthImage() && secondFileExists) {
                    label.setImage2(holder.getCroppedImage2());
                }
                if (label.getMouseListeners().length == 0) {
                    label.addMouseListener(new PanelListener(holder, label));
                }
                holder.setLabel(label);
                pollMakerPanel.add(label);
                pollRows.add(holder);
                GUI.getMainPanel()
                        .setPollMakerPanelSize();
            }
            refreshTextListModel();
            refreshPollMakerComponents();
            if (!error) {
                JOptionPane.showMessageDialog(GUI.getInstance(), "Progress loaded successfully.", "Success!",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(GUI.getInstance(), "Something went wrong while loading. Tell Ant he's an idiot.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_loadProgressButtonActionPerformed

    private void loadCropButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadCropButtonActionPerformed
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel panel = new JPanel(new BorderLayout());
        int width = 1250;
        int heightPerImg = 306;
        int scrollBarWidth = (int) UIManager.get("ScrollBar.width");
        int scrollPaneWidth = width + scrollBarWidth;
        scrollPane.setMinimumSize(new Dimension(scrollPaneWidth, heightPerImg * 2));
        scrollPane.setPreferredSize(new Dimension(scrollPaneWidth, heightPerImg * 2));
        scrollPane.setMaximumSize(new Dimension(scrollPaneWidth, heightPerImg * 2));
        panel.setMinimumSize(new Dimension(width + 100, (heightPerImg * 2) + 100));
        panel.setMaximumSize(new Dimension(width + 100, (heightPerImg * 2) + 100));
        panel.setPreferredSize(new Dimension(width + 100, (heightPerImg * 2) + 100));
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        JLabel label = new JLabel();
        label.setFont(new java.awt.Font("Tahoma", 1, 12));
        label.setForeground(Color.RED);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setAlignmentY(JLabel.CENTER_ALIGNMENT);
        label.setText("Select the image you want to add.");
        label.setPreferredSize(new Dimension(width, 35));
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel childPanel = new JPanel();
        int numSinglePerRow = 4;
        int currentRowCounter = 1;
        int currentCount = 0;
        childPanel.setMinimumSize(new Dimension(width, heightPerImg));
        childPanel.setMaximumSize(new Dimension(width, heightPerImg * 25));
        childPanel.setPreferredSize(new Dimension(width, heightPerImg));
        CropSelectionListener.cropSelectionPanel = childPanel;
        scrollPane.setViewportView(childPanel);
        File croppedImageFolder = new File(System.getProperty("user.dir")
                .concat("/croppedimages"));
        LOGGER.info(croppedImageFolder.getAbsolutePath());
        if (!croppedImageFolder.exists()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "No cropped images are saved.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        File[] croppedImageFiles = croppedImageFolder.listFiles();
        LOGGER.info(croppedImageFiles.length);

        if (croppedImageFiles.length == 0) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "No cropped images are saved.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (File file : croppedImageFiles) {
            if (!ImageTools.isImageFile(file.getAbsolutePath())) {
                continue;
            }
            if (MainPanel.inMiddleOfPanel && ((int) ImageTools.getImageDimensionsFromFile(file)
                    .getWidth()) > 300) {
                continue;
            }
            BufferedImage img;
            try {
                img = ImageIO.read(file);
            } catch (Exception e) {
                LOGGER.info("Error reading cropped image, not displaying.", e);
                continue;
            }
            if (img == null) {
                continue;
            }
            if (currentCount == numSinglePerRow) {
                currentCount = 0;
                currentRowCounter++;
            }
            if (img.getWidth() > 300) {
                currentCount += 2;
            } else {
                currentCount++;
            }
            JLabel imageLabel = new JLabel();
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setSize(new Dimension(img.getWidth(), img.getHeight()));
            imageLabel.setMaximumSize(new Dimension(img.getWidth(), img.getHeight()));
            imageLabel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
            imageLabel.addMouseListener(new CropSelectionListener(imageLabel, img));
            childPanel.add(imageLabel);
            int height = currentRowCounter * heightPerImg + 5;
            childPanel.setMinimumSize(new Dimension(width, height));
            childPanel.setPreferredSize(new Dimension(width, height));
            childPanel.setMaximumSize(new Dimension(width, height));
            childPanel.setSize(width, height);
            childPanel.revalidate();
            childPanel.repaint();
        }
        panel.revalidate();
        panel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
        int res = JOptionPane.showConfirmDialog(null, panel, "Cropped Image Selection", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            if (CropSelectionListener.selectedImage != null) {
                DNDListener.displayImage(CropSelectionListener.selectedImage, true);
            }
        }

    }//GEN-LAST:event_loadCropButtonActionPerformed

    private void saveSubImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSubImageActionPerformed
        saveSubImage();
    }//GEN-LAST:event_saveSubImageActionPerformed

    private void clearImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearImageActionPerformed
        int res = JOptionPane.showConfirmDialog(GUI.getInstance(), "Are you sure you want to clear the poll image? This action cannot be undone!",
                "Confirm clear",
                JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            for (PollRowHolder holder : pollRows) {
                pollMakerPanel.remove(holder.getLabel());
            }
            pollRows = new ArrayList<>();
            refreshPollMakerComponents();
        }
    }//GEN-LAST:event_clearImageActionPerformed

    private void omitNumbersToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_omitNumbersToggleButtonItemStateChanged
        Config.OMIT_NUMBERS = omitNumbersToggleButton.isSelected();
        refreshPollMakerComponents();
    }//GEN-LAST:event_omitNumbersToggleButtonItemStateChanged

    public void refreshPollMakerComponents() {
        pollMakerScrollPane.revalidate();
        pollMakerPanel.revalidate();
        pollMakerScrollPane.repaint();
        pollMakerPanel.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearImage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton loadCropButton;
    private javax.swing.JButton loadProgressButton;
    private javax.swing.JToggleButton omitNumbersToggleButton;
    protected javax.swing.JPanel pollMakerPanel;
    protected javax.swing.JScrollPane pollMakerScrollPane;
    private javax.swing.JButton saveImage;
    private javax.swing.JButton saveProgressButton;
    private javax.swing.JButton saveSubImage;
    // End of variables declaration//GEN-END:variables
}
