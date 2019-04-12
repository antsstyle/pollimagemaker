/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.tools;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class FileTools {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     * @param filepath
     * @return
     */
    public static String getFileMD5(String filepath) {
        File imageFile = new File(filepath);
        try {
            HashCode md5 = com.google.common.io.Files.hash(imageFile, Hashing.md5());
            String md5String = md5.toString();
            return md5String;
        } catch (Exception e) {
            LOGGER.error("Could not get MD5 for filepath: " + filepath, e);
            return null;
        }
    }
    
    public static String getExtensionWithoutDot(String filePath) {
        int index = filePath.lastIndexOf(".");
        if (index == -1) {
            return null;
        } else {
            return filePath.substring(index+1, filePath.length());
        }
    }

}
