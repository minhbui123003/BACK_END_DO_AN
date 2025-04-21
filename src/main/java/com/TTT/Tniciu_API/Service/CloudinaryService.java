package com.TTT.Tniciu_API.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryService {
    private Cloudinary cloudinary;

    public CloudinaryService(String cloudName, String apiKey, String apiSecret) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        cloudinary = new Cloudinary(config);
    }

    public String uploadImage(File imageFile) throws IOException {
        // Upload image to Cloudinary
        Map<?, ?> result = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap());
        // Return the secure URL of the uploaded image
        return (String) result.get("secure_url");
    }
}
