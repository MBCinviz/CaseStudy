package com.hipicon.casestudy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String sellerName;

    @NotEmpty
    private List<String> imageUrls;

    @Min(0)
    private BigDecimal price;

    @Min(0)
    private Integer stock;

    @NotBlank
    @Size(max = 200)
    private String description;
}
