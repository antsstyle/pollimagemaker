/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.datastructures;

import antsstyle.pollimagemaker.GUI.CustomLabel;
import antsstyle.pollimagemaker.GUI.MainPanel;
import java.awt.image.BufferedImage;

/**
 *
 * @author Ant
 */
public class PollRowHolder {
    
    public static int getNewID() {
        if (MainPanel.pollRows.isEmpty()) {
            return 0;
        }
        else {
            int max = 0;
            for (PollRowHolder holder: MainPanel.pollRows) {
                int i = holder.getId();
                if (i > max) {
                    max = i;
                }
            }
            return max+1;
        }
    }
    private BufferedImage croppedImage1;
    private BufferedImage croppedImage2;
    private boolean dualWidthImage = false;
    
    private int id;
    private CustomLabel label;
    private String pollText;
    public PollRowHolder() {
        
    }
    public BufferedImage getCroppedImage1() {
        return croppedImage1;
    }
    public BufferedImage getCroppedImage2() {
        return croppedImage2;
    }

    public int getId() {
        return id;
    }
    

    public CustomLabel getLabel() {
        return label;
    }
    public String getPollText() {
        return pollText;
    }
    public boolean isDualWidthImage() {
        return dualWidthImage;
    }

    public PollRowHolder setCroppedImage1(BufferedImage croppedImage1) {
        this.croppedImage1 = croppedImage1;
        return this;
    }


    public PollRowHolder setCroppedImage2(BufferedImage croppedImage2) {
        this.croppedImage2 = croppedImage2;
        return this;
    }


    public PollRowHolder setDualWidthImage(boolean dualWidthImage) {
        this.dualWidthImage = dualWidthImage;
        return this;
    }
    public PollRowHolder setId(int id) {
        this.id = id;
        return this;
    }
    public PollRowHolder setLabel(CustomLabel label) {
        this.label = label;
        return this;
    }
    public PollRowHolder setPollText(String pollText) {
        this.pollText = pollText;
        return this;
    }
    
}
