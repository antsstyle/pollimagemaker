/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.datastructures;

import java.awt.image.BufferedImage;

/**
 *
 * @author Ant
 */
public class CropQueueEntry {
    
    private BufferedImage image;
    private boolean fromCrop;
    
    public CropQueueEntry(BufferedImage image, boolean fromCrop) {
        this.image = image;
        this.fromCrop = fromCrop;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public boolean isFromCrop() {
        return fromCrop;
    }
    
}
