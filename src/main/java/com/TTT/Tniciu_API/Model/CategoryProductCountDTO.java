package com.TTT.Tniciu_API.Model;

public class CategoryProductCountDTO {
    private String categoryName;
    private long productCount;

    // Constructor
    public CategoryProductCountDTO(String categoryName, long productCount) {
        this.categoryName = categoryName;
        this.productCount = productCount;
    }

    // Getters and setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }
}