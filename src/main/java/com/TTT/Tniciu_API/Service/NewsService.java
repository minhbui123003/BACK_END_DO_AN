package com.TTT.Tniciu_API.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.TTT.Tniciu_API.Model.New;
import com.TTT.Tniciu_API.Repository.NewsRepository;

@Service
public class NewsService {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private NewsRepository newsRepository;

    public List<New> getAllNews() {
        return newsRepository.findAll();
    }

    public New getNewsById(Long id) {
        return newsRepository.findById(id).orElse(null);
    }

    private String extractBase64String(String dataUrl) {
        String base64Image = dataUrl.split(",")[1];
        return base64Image;
    }

    public New saveNews(New news) {
        if (news.getImage() != null && !news.getImage().isEmpty()) {
            try {
                String imageUrlString = news.getImage();
                File imageFile;

                if (imageUrlString.startsWith("data:")) {
                    // Handle data URL
                    String base64Image = extractBase64String(imageUrlString);
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    // Create temporary file
                    imageFile = File.createTempFile("image", ".tmp");
                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                        fos.write(imageBytes);
                    }
                } else {
                    // Handle normal URL
                    URL imageUrl = new URL(imageUrlString);
                    imageFile = File.createTempFile("image", ".tmp");

                    try (InputStream in = imageUrl.openStream()) {
                        Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // Upload image to Cloudinary
                String newImageUrl = cloudinaryService.uploadImage(imageFile);

                // Delete temporary file
                imageFile.delete();

                // Update image URL in the database
                news.setImage(newImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newsRepository.save(news);
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    // New method for updating news
    public New updateNews(Long id, New newsDetails) {
        New existingNews = newsRepository.findById(id).orElse(null);
        if (existingNews != null) {
            existingNews.setTitle(newsDetails.getTitle());
            existingNews.setDate(newsDetails.getDate());
            existingNews.setDescription(newsDetails.getDescription());

            if (newsDetails.getImage() != null && !newsDetails.getImage().isEmpty()) {
                try {
                    String imageUrlString = newsDetails.getImage();
                    File imageFile;

                    if (imageUrlString.startsWith("data:")) {
                        // Handle data URL
                        String base64Image = extractBase64String(imageUrlString);
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                        // Create temporary file
                        imageFile = File.createTempFile("image", ".tmp");
                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            fos.write(imageBytes);
                        }
                    } else {
                        // Handle normal URL
                        URL imageUrl = new URL(imageUrlString);
                        imageFile = File.createTempFile("image", ".tmp");

                        try (InputStream in = imageUrl.openStream()) {
                            Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    // Upload image to Cloudinary
                    String newImageUrl = cloudinaryService.uploadImage(imageFile);

                    // Delete temporary file
                    imageFile.delete();

                    // Update image URL in the database
                    existingNews.setImage(newImageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return newsRepository.save(existingNews);
        } else {
            return null;
        }
    }
}
