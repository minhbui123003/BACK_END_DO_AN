package com.TTT.Tniciu_API.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

//
public class ImageUtils {

    public static File decodeBase64Image(String base64Image, String outputFilePath) throws Exception {
        // Decode base64 string to byte array
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // Write byte array to file
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(imageBytes);
        }

        // Return the created file
        return new File(outputFilePath);
    }
}
