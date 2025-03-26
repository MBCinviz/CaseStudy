package com.hipicon.casestudy.dto;

import com.hipicon.casestudy.entity.Product;
import lombok.Data;

@Data
public class ProductStatusRequest {
    private Product.ProductStatus status;
}
