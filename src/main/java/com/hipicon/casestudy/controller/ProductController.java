package com.hipicon.casestudy.controller;

import com.hipicon.casestudy.dto.*;
import com.hipicon.casestudy.entity.Product;
import com.hipicon.casestudy.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductDto newProduct = productService.addProduct(productRequest);
        return ResponseEntity.ok(newProduct);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Product> updateProductStatus(@PathVariable Long id,
                                                       @RequestBody ProductStatusRequest statusRequest) {
        Product updatedProduct = productService.updateProductStatus(id, statusRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getUserProducts() {
        List<Product> products = productService.getUserProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<ProductDto>> getFilteredProducts(@RequestBody ProductFilterDto dto) {
        Page<ProductDto> products = productService.getFilteredProducts(dto);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashBoardResponse> getUserDashboard() {
        DashBoardResponse dashboard = productService.getUserDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/samenameproducts")
    public ResponseEntity<?> getSameNameProducts(@RequestParam String name) {
        List<ProductDto> products = productService.getSameNameProducts(name);

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorDto("Ürüne ait kayıt bulunamadı"));
        }
        return ResponseEntity.ok(products);
    }
}
