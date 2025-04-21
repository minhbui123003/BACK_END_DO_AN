package com.TTT.Tniciu_API.Controller;

import com.TTT.Tniciu_API.Model.Product;
import com.TTT.Tniciu_API.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    private final ProductService productService;

    @Autowired
    public DiscountController(ProductService productService) {
        this.productService = productService;
    }

    // Lấy danh sách sản phẩm giảm giá
    @GetMapping("/products")
    public List<Product> getDiscountedProducts() {
        return productService.getDiscountedProducts();
    }

    // Xóa giảm giá hết hạn
    @DeleteMapping("/remove-expired")
    public ResponseEntity<Void> removeExpiredDiscounts() {
        productService.removeExpiredDiscounts();
        return ResponseEntity.noContent().build();
    }
}
