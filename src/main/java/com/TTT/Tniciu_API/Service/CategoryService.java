// CategoryService.java
package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Category;
import com.TTT.Tniciu_API.Model.CategoryProductCountDTO;
import com.TTT.Tniciu_API.Model.Product;
import com.TTT.Tniciu_API.Repository.CategoryRepository;
import com.TTT.Tniciu_API.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(Long id, Category categoryDetails) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            category.setDiscount(categoryDetails.getDiscount());
            category.setDiscountExpiration(categoryDetails.getDiscountExpiration());

            // Cập nhật giá cho tất cả sản phẩm thuộc danh mục này
            List<Product> products = productRepository.findByCategoryId(id);
            for (Product product : products) {
                product.setDiscount(categoryDetails.getDiscount());
                product.setDiscountExpiration(categoryDetails.getDiscountExpiration());
                productRepository.save(product);
            }

            return categoryRepository.save(category);
        });
    }

    public void deleteCategory(Long id) {
        long productCount = productRepository.countByCategoryId(id);

        if (productCount > 0) {
            throw new IllegalStateException("Có " + productCount + " sản phẩm thuộc danh mục này. Không được phép xoá!");
        }

        categoryRepository.deleteById(id);
    }

    public List<CategoryProductCountDTO> getCategoryProductCounts() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(category -> {
            long productCount = productRepository.countByCategoryId(category.getId());
            return new CategoryProductCountDTO(category.getName(), productCount);
        }).collect(Collectors.toList());
    }
}
