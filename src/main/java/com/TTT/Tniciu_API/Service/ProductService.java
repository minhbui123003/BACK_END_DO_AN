package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Product;
import com.TTT.Tniciu_API.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Lấy tất cả sản phẩm chưa xóa, chưa hết hạn sử dụng, và số lượng lớn hơn 0 (cho khách hàng)
    public List<Product> getAllProducts() {
        return productRepository.findAll().stream()
                .filter(product -> !product.isDeleted() &&
                        (product.getExpirationDate() == null || product.getExpirationDate().isAfter(LocalDateTime.now())) &&
                        product.getQuantity() > 0)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID chưa xóa (cho khách hàng)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> !product.isDeleted() &&
                        (product.getExpirationDate() == null || product.getExpirationDate().isAfter(LocalDateTime.now())));
    }

    // Lấy tất cả sản phẩm cho admin (bao gồm cả sản phẩm đã xóa mềm)
    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    // Tạo sản phẩm mới
    public Product createProduct(Product product) {
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                String imageUrlString = product.getImage();
                File imageFile;

                if (imageUrlString.startsWith("data:")) {
                    // Xử lý URL dữ liệu
                    String base64Image = extractBase64String(imageUrlString);
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    // Tạo tệp tạm thời
                    imageFile = File.createTempFile("image", ".tmp");
                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                        fos.write(imageBytes);
                    }
                } else {
                    // Xử lý URL thông thường
                    URL imageUrl = new URL(imageUrlString);
                    imageFile = File.createTempFile("image", ".tmp");

                    try (InputStream in = imageUrl.openStream()) {
                        Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // Tải hình ảnh lên Cloudinary
                String newImageUrl = cloudinaryService.uploadImage(imageFile);

                // Xóa tệp tạm thời
                imageFile.delete();

                // Cập nhật URL hình ảnh trong cơ sở dữ liệu
                product.setImage(newImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return productRepository.save(product);
    }

    private String extractBase64String(String dataUrl) {
        return dataUrl.split(",")[1];
    }

    // Cập nhật sản phẩm (admin)
    public Optional<Product> updateProduct(Long id, Product proDuct) {
        return productRepository.findById(id).map(product -> {
            product.setName(proDuct.getName());
            product.setPrice(proDuct.getPrice());
            product.setDescription(proDuct.getDescription());
            product.setQuantity(proDuct.getQuantity());
            if (proDuct.getImage() != null && !proDuct.getImage().isEmpty()) {
                try {
                    String imageUrlString = proDuct.getImage();
                    File imageFile;

                    if (imageUrlString.startsWith("data:")) {
                        String base64Image = extractBase64String(imageUrlString);
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                        imageFile = File.createTempFile("image", ".tmp");
                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            fos.write(imageBytes);
                        }
                    } else {
                        URL imageUrl = new URL(imageUrlString);
                        imageFile = File.createTempFile("image", ".tmp");
                        try (InputStream in = imageUrl.openStream()) {
                            Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    String newImageUrl = cloudinaryService.uploadImage(imageFile);
                    imageFile.delete();
                    product.setImage(newImageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            product.setSupplier(product.getSupplier());
            product.setCategory(proDuct.getCategory());
            product.setExpirationDate(proDuct.getExpirationDate());
            product.setDiscount(proDuct.getDiscount());
            product.setDiscountExpiration(proDuct.getDiscountExpiration());
            return productRepository.save(product);
        });
    }

    // Xóa sản phẩm hoàn toàn (admin)
    public void deleteProduct(Long id) {
        if (productRepository.existsById(id)) { // Kiểm tra xem sản phẩm có tồn tại không
            productRepository.deleteById(id); // Xóa sản phẩm khỏi cơ sở dữ liệu
        } else {
            throw new RuntimeException("Product not found with ID: " + id); // Thông báo nếu không tìm thấy sản phẩm
        }
    }

    // Lấy các sản phẩm đang giảm giá và chưa hết hạn sử dụng (cho khách hàng)
    public List<Product> getDiscountedProducts() {
        return productRepository.findByDiscountGreaterThanAndDiscountExpirationAfter(0, LocalDateTime.now()).stream()
                .filter(product -> !product.isDeleted() &&
                        (product.getExpirationDate() == null || product.getExpirationDate().isAfter(LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    // Xóa giảm giá cho sản phẩm nếu hết hạn và xóa mềm sản phẩm nếu hết hạn sử dụng (admin)
    public void removeExpiredDiscounts() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            boolean discountExpired = product.getDiscountExpiration() != null &&
                    product.getDiscountExpiration().isBefore(LocalDateTime.now());
            boolean productExpired = product.getExpirationDate() != null &&
                    product.getExpirationDate().isBefore(LocalDateTime.now());

            if (discountExpired) {
                product.setDiscount(0); // Xóa giảm giá nếu đã hết hạn
                product.setDiscountExpiration(null);
            }

            if (productExpired) {
                product.setDeleted(true); // Đánh dấu sản phẩm là đã hết hạn
            }

            productRepository.save(product); // Lưu sản phẩm
        }
    }
}
