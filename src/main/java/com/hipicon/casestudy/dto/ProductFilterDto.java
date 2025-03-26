package com.hipicon.casestudy.dto;

import com.hipicon.casestudy.entity.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductFilterDto {
    private Integer page;
    private String searchTerm;
    private List<Product.ProductStatus> status;
}
