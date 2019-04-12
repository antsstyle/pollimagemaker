/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import antsstyle.pollimagemaker.configuration.Config;
import antsstyle.pollimagemaker.configuration.DefaultConfig;
import antsstyle.pollimagemaker.datastructures.CropQueueEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class ImageCropDialogFrame extends javax.swing.JFrame {

    private static final Logger LOGGER = LogManager.getLogger();

    private Rectangle captureRect;
    private boolean dualWidthImage;
    private JLabel screenLabel;
    private boolean squareCrop = true;
    private JCheckBox checkBox;
    private int dialogResult = -1;

    public static boolean CURRENTLY_CROPPING = false;

    public static final ArrayList<CropQueueEntry> queue = new ArrayList<>();

    public static void begin() {
        if (!CURRENTLY_CROPPING && !queue.isEmpty()) {
            CropQueueEntry first = queue.get(0);
            queue.remove(0);
            CURRENTLY_CROPPING = true;
            new ImageCropDialogFrame(first.getImage(), first.isFromCrop());
        }
    }

    /**
     * Creates new form ImageCropDialogFrame
     *
     * @param image
     * @param fromCrop
     */
    public ImageCropDialogFrame(final BufferedImage image, boolean fromCrop) {
        initComponents();
        final BufferedImage imageCopy = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                image.getType());
        if (imageCopy.getWidth() < 300 || imageCopy.getHeight() < 300) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "This image is too small. Please drop an image bigger than 300x300 here.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            performUpdate(null, false, null);
            return;
        }

        screenLabel = new JLabel(new ImageIcon(imageCopy));
        JScrollPane screenScroll = new JScrollPane(screenLabel);
        screenScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        screenScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        screenScroll.setPreferredSize(new Dimension(
                (int) (image.getWidth()),
                (int) (image.getHeight())));
        screenScroll.setMaximumSize(new Dimension(
                (int) (image.getWidth()),
                (int) (image.getHeight())));
        screenScroll.setMinimumSize(new Dimension(
                (int) (image.getWidth()),
                (int) (image.getHeight())));
        dialogPanel = new JPanel(new BorderLayout());
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(screenScroll, BorderLayout.CENTER);
        JPanel leftEmptyPanel = new JPanel();
        JPanel rightEmptyPanel = new JPanel();
        JPanel topEmptyPanel = new JPanel();
        JPanel bottomEmptyPanel = new JPanel();
        dialogPanel.add(containerPanel, BorderLayout.CENTER);
        checkBox = new JCheckBox();
        checkBox.setText("Dual-width image");
        checkBox.setSelected(false);
        checkBox.setFont(new java.awt.Font("Tahoma", 1, 12));
        checkBox.addActionListener((ActionEvent ae) -> {
            if (MainPanel.inMiddleOfPanel) {
                checkBox.setSelected(false);
            }
            squareCrop = !checkBox.isSelected();
            captureRect = null;
            repaint(image, imageCopy);
            dialogPanel.revalidate();
            dialogPanel.repaint();
        });
        JPanel optionsPanel = new JPanel(new BorderLayout());
        JPanel checkBoxSubPanel = new JPanel();
        checkBoxSubPanel.add(checkBox);
        optionsPanel.add(checkBoxSubPanel, BorderLayout.NORTH);
        JPanel optionsMenuSubPanel = new JPanel();
        JButton okOption = new JButton("OK");
        okOption.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
        Dimension d = new Dimension(Config.CROP_BUTTONS_WIDTH, Config.CROP_BUTTONS_HEIGHT);
        okOption.setMinimumSize(d);
        okOption.setMaximumSize(d);
        okOption.setPreferredSize(d);
        okOption.addActionListener((ActionEvent ae) -> {
            dualWidthImage = checkBox.isSelected();
            dialogResult = 1;
            SwingUtilities.invokeLater(() -> {
                this.setVisible(false);
            });
            performUpdate(image, fromCrop, captureRect);
        });
        JButton cancelOption = new JButton("Cancel");
        cancelOption.setMinimumSize(d);
        cancelOption.setMaximumSize(d);
        cancelOption.setPreferredSize(d);
        cancelOption.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
        cancelOption.addActionListener((ActionEvent ae) -> {
            dualWidthImage = checkBox.isSelected();
            dialogResult = 2;
            SwingUtilities.invokeLater(() -> {
                this.setVisible(false);
            });
            performUpdate(image, fromCrop, captureRect);
        });
        optionsMenuSubPanel.add(okOption);
        optionsMenuSubPanel.add(cancelOption);
        optionsPanel.add(optionsMenuSubPanel, BorderLayout.CENTER);
        dialogPanel.add(optionsPanel, BorderLayout.SOUTH);
        double aspectRatio = (double) imageCopy.getWidth() / (double) imageCopy.getHeight();
        JLabel label = new JLabel();
        label.setFont(new java.awt.Font("Tahoma", 1, 12));
        label.setMinimumSize(new Dimension(image.getWidth(), 35));
        label.setPreferredSize(new Dimension(image.getWidth(), 35));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if (aspectRatio <= DefaultConfig.SQUARE_ASPECTR_LOWER_LIM || (aspectRatio >= DefaultConfig.SQUARE_ASPECTR_HIGHER_LIM
                && aspectRatio < DefaultConfig.RECT_ASPECTR_LOWER_LIM) || aspectRatio >= DefaultConfig.RECT_ASPECTR_HIGHER_LIM) {
            label.setForeground(Color.RED);
            label.setText("<html>This image exceeds the Auto-Crop threshold. You must select a region to add it.</html>");
        } else if (MainPanel.inMiddleOfPanel && (aspectRatio >= Config.RECT_ASPECTR_LOWER_LIM && aspectRatio <= Config.RECT_ASPECTR_HIGHER_LIM)
                && Config.AUTO_CROP) {
            label.setForeground(Color.RED);
            label.setText("<html>This image can't be autocropped as a square image. You must select a square region to add it.</html>");
        } else {
            label.setForeground(Color.BLACK);
            label.setText("<html>Select a region to add, or press OK without selecting and the app will crop the center of the image.</html>");
        }
        dialogPanel.add(label, BorderLayout.NORTH);
        setContentPane(dialogPanel);
        repaint(image, imageCopy);
        screenLabel.repaint();
        MouseAdapter adapter = new MouseAdapter() {

            private Point cursorOrigin = new Point();
            private Point pivotPoint;
            private final int resizeForgivenessThreshold = 5;
            private boolean mouseDown = false;

            private int cursorModeSelected = 0;

            @Override
            public void mousePressed(MouseEvent me) {
                mouseDown = true;
                Point clickPoint = me.getPoint();
                cursorOrigin = clickPoint;
                determineMode(cursorOrigin);
                switch (cursorModeSelected) {
                    case Cursor.DEFAULT_CURSOR:
                        captureRect = null;
                        pivotPoint = new Point(clickPoint.x, clickPoint.y);
                        break;
                    case Cursor.HAND_CURSOR:
                        pivotPoint = new Point(clickPoint.x - captureRect.x, clickPoint.y - captureRect.y);
                        break;
                    case Cursor.SE_RESIZE_CURSOR:
                        pivotPoint = new Point(captureRect.x, captureRect.y);
                        break;
                    case Cursor.NW_RESIZE_CURSOR:
                        pivotPoint = new Point(captureRect.x + captureRect.width, captureRect.y + captureRect.height);
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        pivotPoint = new Point(0, captureRect.y);
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        pivotPoint = new Point(captureRect.x, captureRect.y + captureRect.height);
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        pivotPoint = new Point(0, captureRect.y + captureRect.height);
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        pivotPoint = new Point(captureRect.x + captureRect.width, 0);
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        pivotPoint = new Point(captureRect.x, 0);
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        pivotPoint = new Point(captureRect.x + captureRect.width, captureRect.y);
                    default:
                        break;
                }
                repaint(image, imageCopy);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mouseDown = false;
                cursorModeSelected = Cursor.DEFAULT_CURSOR;
                screenLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseMoved(MouseEvent me) {
                cursorOrigin = me.getPoint();
                if (!mouseDown) {
                    determineMode(cursorOrigin);
                }
                repaint(image, imageCopy);
                screenLabel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent me) {
                Point clickPoint = me.getPoint();
                if (!mouseDown) {
                    determineMode(clickPoint);
                }
                int x;
                int y;
                switch (cursorModeSelected) {
                    case Cursor.HAND_CURSOR:
                        int x1 = clickPoint.x - pivotPoint.x;
                        int y1 = clickPoint.y - pivotPoint.y;
                        int x2 = captureRect.width - pivotPoint.x;
                        int y2 = captureRect.height - pivotPoint.y;
                        if (x1 < 0) {
                            x1 = 0;
                        }
                        if (y1 < 0) {
                            y1 = 0;
                        }
                        if (clickPoint.x + x2 > image.getWidth()) {
                            x1 = image.getWidth() - captureRect.width;
                        }
                        if (clickPoint.y + y2 > image.getHeight()) {
                            y1 = image.getHeight() - captureRect.height;
                        }
                        Dimension d = new Dimension(captureRect.width, captureRect.height);
                        captureRect = new Rectangle(new Point(x1, y1),
                                d);
                        repaint(image, imageCopy);
                        screenLabel.repaint();
                        break;
                    case Cursor.DEFAULT_CURSOR:
                    case Cursor.NE_RESIZE_CURSOR:
                    case Cursor.SE_RESIZE_CURSOR:
                    case Cursor.NW_RESIZE_CURSOR:
                    case Cursor.SW_RESIZE_CURSOR:
                        x = clickPoint.x - pivotPoint.x;
                        y = clickPoint.y - pivotPoint.y;
                        if (clickPoint.x < 0 || clickPoint.y < 0) {
                            return;
                        }
                        Point originPoint = new Point(Math.min(pivotPoint.x, clickPoint.x), Math.min(pivotPoint.y, clickPoint.y));
                        boolean cthulhu = false;
                        int z = 0;
                        // Correct code
                        if (x < 0 && y < 0) {
                            cthulhu = true;
                            if (squareCrop) {
                                z = pivotPoint.y - clickPoint.y;
                                originPoint = new Point(pivotPoint.x - z, pivotPoint.y - z);
                            } else {
                                z = pivotPoint.y - clickPoint.y;
                                originPoint = new Point((pivotPoint.x - 2 * z), pivotPoint.y - z);
                            }
                        } else if (x < 0 && y > 0) {
                            cthulhu = true;
                            if (squareCrop) {
                                z = pivotPoint.x - clickPoint.x;
                                originPoint = new Point(pivotPoint.x - z, pivotPoint.y);
                            } else {
                                z = pivotPoint.x - clickPoint.x;
                                originPoint = new Point((pivotPoint.x - 2 * z), pivotPoint.y);
                            }
                        } else if (y < 0 && x > 0) {
                            cthulhu = true;
                            if (squareCrop) {
                                z = pivotPoint.y - clickPoint.y;
                                originPoint = new Point(pivotPoint.x, pivotPoint.y - z);
                            } else {
                                z = pivotPoint.y - clickPoint.y;
                                originPoint = new Point((pivotPoint.x), pivotPoint.y - z);
                            }
                        }
                        //LOGGER.info(z);
                        //LOGGER.info(originPoint);
                        //Correct code
                        /*if (squareCrop) {
                            if (x > y) {
                                x = y;
                            }
                            if (y > x) {
                                y = x;
                            }
                        } else {
                            if (x > 2 * y) {
                                x = 2 * y;
                            }
                            if (y > x / 2) {
                                y = x / 2;
                            }
                        }*/
                        if (squareCrop) {
                            if (x > y) {
                                y = x;
                            }
                            if (y > x) {
                                x = y;
                            }
                        } else {
                            if (x > 2 * y) {
                                y = x / 2;
                            }
                            if (y > x / 2) {
                                x = 2 * y;
                            }
                        }
                        x = Math.abs(x);
                        y = Math.abs(y);
                        Rectangle result;

                        if (!cthulhu) {
                            result = new Rectangle(originPoint,
                                    new Dimension(x, y));
                        } else if (squareCrop) {
                            result = new Rectangle(originPoint,
                                    new Dimension(z, z));
                        } else {
                            result = new Rectangle(originPoint,
                                    new Dimension(2 * z, z));
                        }

                        if ((result.x + result.width) > image.getWidth() || (result.y + result.height) > image.getHeight()
                                || result.x < 0 || result.y < 0) {
                            return;
                        }
                        captureRect = result;
                        repaint(image, imageCopy);
                        screenLabel.repaint();
                        break;
                }
            }

            private void determineMode(Point cursor) {
                if (captureRect == null) {
                    cursorModeSelected = Cursor.DEFAULT_CURSOR;
                } else if (cursor.x > (captureRect.x + 2 * resizeForgivenessThreshold)
                        && cursor.x < (captureRect.x + captureRect.width - 2 * resizeForgivenessThreshold)
                        && cursor.y > (captureRect.y + 2 * resizeForgivenessThreshold)
                        && cursor.y < (captureRect.y + captureRect.height - 2 * resizeForgivenessThreshold)) {
                    cursorModeSelected = Cursor.HAND_CURSOR;
                } else if (cursor.x >= (captureRect.x)
                        && cursor.x < (captureRect.x + resizeForgivenessThreshold)
                        && cursor.y >= (captureRect.y)
                        && cursor.y < (captureRect.y + resizeForgivenessThreshold)) {
                    cursorModeSelected = Cursor.NW_RESIZE_CURSOR;
                } else if (cursor.x >= (captureRect.x + captureRect.width - resizeForgivenessThreshold)
                        && cursor.x <= (captureRect.x + captureRect.width)
                        && cursor.y >= (captureRect.y)
                        && cursor.y <= (captureRect.y + resizeForgivenessThreshold)) {
                    cursorModeSelected = Cursor.NE_RESIZE_CURSOR;
                } else if (cursor.x >= (captureRect.x)
                        && cursor.x <= (captureRect.x + resizeForgivenessThreshold)
                        && cursor.y >= (captureRect.y + captureRect.height - resizeForgivenessThreshold)
                        && cursor.y <= (captureRect.y + captureRect.height)) {
                    cursorModeSelected = Cursor.SW_RESIZE_CURSOR;
                } else if (cursor.x >= (captureRect.x + captureRect.width - resizeForgivenessThreshold)
                        && cursor.x <= (captureRect.x + captureRect.width)
                        && cursor.y >= (captureRect.y + captureRect.height - resizeForgivenessThreshold)
                        && cursor.y <= (captureRect.y + captureRect.height)) {
                    cursorModeSelected = Cursor.SE_RESIZE_CURSOR;
                } else {
                    cursorModeSelected = Cursor.DEFAULT_CURSOR;
                }
                screenLabel.setCursor(new Cursor(cursorModeSelected));
            }
        };
        screenLabel.addMouseListener(adapter);
        screenLabel.addMouseMotionListener(adapter);
        this.pack();
        this.revalidate();
        this.repaint();
        this.setLocationRelativeTo(null);
        int widthDifference = dialogPanel.getSize().width - image.getWidth();
        int heightDifference = dialogPanel.getSize().height - image.getHeight();
        if (heightDifference > 150) {
            if (heightDifference % 2 == 0) {
                topEmptyPanel.setPreferredSize(new Dimension(image.getWidth(), heightDifference / 2));
                bottomEmptyPanel.setPreferredSize(new Dimension(image.getWidth(), heightDifference / 2));
            } else {
                topEmptyPanel.setPreferredSize(new Dimension(image.getWidth(), heightDifference / 2));
                bottomEmptyPanel.setPreferredSize(new Dimension(image.getWidth(), (heightDifference / 2)+1));
            }
            containerPanel.add(topEmptyPanel, BorderLayout.NORTH);
            containerPanel.add(bottomEmptyPanel, BorderLayout.SOUTH);
        }
        if (widthDifference > 0) {
            if (widthDifference % 2 == 0) {
                leftEmptyPanel.setPreferredSize(new Dimension(widthDifference/2, image.getHeight()));
                rightEmptyPanel.setPreferredSize(new Dimension(widthDifference/2, image.getHeight()));
            } else {
                leftEmptyPanel.setPreferredSize(new Dimension(widthDifference/2, image.getHeight()));
                rightEmptyPanel.setPreferredSize(new Dimension((widthDifference/2)+1, image.getHeight()));
            }
            containerPanel.add(leftEmptyPanel, BorderLayout.LINE_START);
            containerPanel.add(rightEmptyPanel, BorderLayout.LINE_END);
        }
        showDialog();
    }

    private void showDialog() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }

    private void performUpdate(BufferedImage img, boolean fromCrop, Rectangle captureRect) {
        if (dialogResult == 1) {
            DNDListener.processImage(img, fromCrop, captureRect, dualWidthImage);
        }
        if (queue.isEmpty()) {
            CURRENTLY_CROPPING = false;
        } else {
            CropQueueEntry first = queue.get(0);
            queue.remove(0);
            new ImageCropDialogFrame(first.getImage(), first.isFromCrop());
        }
    }

    public final void repaint(BufferedImage orig, BufferedImage copy) {
        Graphics2D g = copy.createGraphics();
        g.drawImage(orig, 0, 0, null);
        if (captureRect != null) {
            g.setColor(Color.RED);
            g.draw(captureRect);
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(captureRect);
        }
        g.dispose();
    }

    public int getDialogResult() {
        return dialogResult;
    }

    public void setDialogResult(int dialogResult) {
        this.dialogResult = dialogResult;
    }

    public boolean isDualWidthImage() {
        return dualWidthImage;
    }

    public void setDualWidthImage(boolean dualWidthImage) {
        this.dualWidthImage = dualWidthImage;
    }

    public Rectangle getCaptureRect() {
        return captureRect;
    }

    public void setCaptureRect(Rectangle captureRect) {
        this.captureRect = captureRect;
    }

    public JLabel getScreenLabel() {
        return screenLabel;
    }

    public void setScreenLabel(JLabel screenLabel) {
        this.screenLabel = screenLabel;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dialogPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        javax.swing.GroupLayout dialogPanelLayout = new javax.swing.GroupLayout(dialogPanel);
        dialogPanel.setLayout(dialogPanelLayout);
        dialogPanelLayout.setHorizontalGroup(
            dialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        dialogPanelLayout.setVerticalGroup(
            dialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setVisible(false);
        if (queue.isEmpty()) {
            CURRENTLY_CROPPING = false;
        }
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dialogPanel;
    // End of variables declaration//GEN-END:variables
}
