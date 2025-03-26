package com.hipicon.casestudy.dto;

import com.hipicon.casestudy.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String sellerName;
    private String productName;
    private String productDescription;
    private List<String> productImage;
    private BigDecimal productPrice;
    private Integer stock;
    private Product.ProductStatus productStatus;
}
