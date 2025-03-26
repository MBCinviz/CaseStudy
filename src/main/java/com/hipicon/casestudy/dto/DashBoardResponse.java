package com.hipicon.casestudy.dto;

import com.hipicon.casestudy.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardResponse {
    private Long totalProducts;
    private Integer totalStock;
    private List<Product> lowStockProducts;
}
