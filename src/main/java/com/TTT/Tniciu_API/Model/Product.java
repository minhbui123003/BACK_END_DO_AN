// Product.java
package com.TTT.Tniciu_API.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên là bắt buộc")
    private String name;

    @Min(value = 1 , message = "Vui lòng nhập giá sản phẩm lớn hơn 0")
    private double price;

    @NotBlank(message = "Chú thích là bắt buộc")
    private String description;

    private String image;

    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    private int quantity;

    private LocalDateTime expirationDate;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private int discount;

    private LocalDateTime discountExpiration;

    // Thêm trường xóa mềm
    private boolean isDeleted = false;

    // Tính toán giá sau khuyến mãi
    public double getDiscountedPrice() {
        double discountedPrice = price;

        if (category != null && category.getDiscount() > 0 &&
                category.getDiscountExpiration() != null &&
                LocalDateTime.now().isBefore(category.getDiscountExpiration())) {
            discountedPrice = price * (1 - category.getDiscount() / 100.0);
        } else if (discount > 0 && discountExpiration != null && LocalDateTime.now().isBefore(discountExpiration)) {
            discountedPrice = price * (1 - discount / 100.0);
        }

        return discountedPrice;
    }
    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

}

