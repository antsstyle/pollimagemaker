/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.GUI;

import antsstyle.pollimagemaker.datastructures.PollRowHolder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class PanelListener extends MouseAdapter {

    private static final Logger LOGGER = LogManager.getLogger();

    private PollRowHolder holder;
    private CustomLabel panel;

    public PanelListener(PollRowHolder holder, CustomLabel panel) {
        super();
        this.holder = holder;
        this.panel = panel;
    }

    private void updateMainPanel() {
        GUI.getMainPanel().pollMakerPanel.remove(holder.getLabel());
        MainPanel.pollRows.remove(holder);
        GUI.getMainPanel().setPollMakerPanelSize();
        MainPanel.updatePollRowPanels();
        MainPanel.inMiddleOfPanel = false;
        MainPanel.refreshTextListModel();
        GUI.getMainPanel().pollMakerPanel.revalidate();
        GUI.getMainPanel().pollMakerScrollPane.revalidate();
        GUI.getMainPanel().pollMakerPanel.repaint();
        GUI.getMainPanel().pollMakerScrollPane.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (!MainPanel.inMiddleOfPanel) {
                int res2 = JOptionPane.showOptionDialog(GUI.getInstance(), "What would you like to do with this poll option?",
                        "Image modification menu", 0, JOptionPane.QUESTION_MESSAGE,
                        null, new String[]{"Edit text for this poll option", "Remove this poll option", "Cancel"}, null);
                switch (res2) {
                    case 0:
                        String text = JOptionPane.showInputDialog(GUI.getInstance(), "Enter new text for this poll option.", holder
                                .getPollText());
                        text = StringUtils.replace(text, " Õ ", " x ");
                        holder.setPollText(text);
                        panel.setPollText(text);
                        panel.revalidate();
                        panel.repaint();
                        MainPanel.refreshTextListModel();
                        break;
                    case 1:
                        updateMainPanel();
                        break;
                    case 2:
                        break;
                    default:
                        LOGGER.error("Unknown option!");
                        break;
                }
            } else {
                int res2 = JOptionPane.showOptionDialog(GUI.getInstance(), "Would you like to remove this image?\n"
                        + "You cannot edit the text for this poll option until you add the second image for it.",
                        "Image modification menu", 0, JOptionPane.QUESTION_MESSAGE,
                        null, new String[]{"Remove this image", "Cancel"}, null);
                switch (res2) {
                    case 0:
                        updateMainPanel();
                        break;
                    case 1:
                        break;
                    default:
                        LOGGER.error("Unknown option!");
                        break;
                }
            }
        }
    }
}
