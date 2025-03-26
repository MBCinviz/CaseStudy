package com.hipicon.casestudy.service;

import com.hipicon.casestudy.dto.*;
import com.hipicon.casestudy.entity.Product;
import com.hipicon.casestudy.entity.User;
import com.hipicon.casestudy.repository.ProductRepository;
import com.hipicon.casestudy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(rollbackOn = Exception.class)
    public ProductDto addProduct(ProductRequest productRequest) {
        User currentUser = getCurrentUser();

        Product product = new Product();

        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        product.setImageUrls(productRequest.getImageUrls());
        product.setStock(productRequest.getStock());
        product.setStatus(Product.ProductStatus.PENDING);
        product.setSellerName(currentUser.getUsername());
        product.setSeller(currentUser);


        product = productRepository.save(product);
        return convertProductToProductDto(product);
    }

    public ProductDto convertProductToProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setProductDescription(product.getDescription());
        productDto.setProductName(product.getName());
        productDto.setProductImage(product.getImageUrls());
        productDto.setProductPrice(product.getPrice());
        productDto.setSellerName(product.getSeller().getUsername());
        productDto.setStock(product.getStock());
        productDto.setProductStatus(product.getStatus());
        return productDto;
    }


    @Transactional(rollbackOn = Exception.class)
    public Product updateProductStatus(Long productId, ProductStatusRequest statusRequest) {
        User currentUser = getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        if (!product.getSeller().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to update this product");
        }

        product.setStatus(statusRequest.getStatus());
        return productRepository.save(product);
    }

    public List<Product> getUserProducts() {
        User currentUser = getCurrentUser();
        return productRepository.findBySeller(currentUser);
    }

    public DashBoardResponse getUserDashboard() {
        User currentUser = getCurrentUser();

        Long totalProducts = productRepository.countBySeller(currentUser);
        Integer totalStock = productRepository.getTotalStockBySeller(currentUser);
        List<Product> lowStockProducts = productRepository.findBySellerAndStockLessThan(currentUser, 1);

        // Handle null total stock (if no products exist)
        if (totalStock == null) {
            totalStock = 0;
        }

        return new DashBoardResponse(totalProducts, totalStock, lowStockProducts);
    }

    public List<ProductDto> getSameNameProducts(String name) {
        List<Product> products = productRepository.getSameNameProducts(name);
        return products.stream()
                .map(this::convertProductToProductDto)
                .collect(Collectors.toList());
    }

    public Page<ProductDto> getFilteredProducts(ProductFilterDto dto) {
        Integer pageNumber = Objects.requireNonNullElse(dto.getPage(), 0);
        Pageable page = PageRequest.of(pageNumber, 3);
        String q = StringUtils.isEmpty(dto.getSearchTerm()) ? null : dto.getSearchTerm().toLowerCase();
        List<Product.ProductStatus> status = CollectionUtils.isEmpty(dto.getStatus()) ? Arrays.stream(Product.ProductStatus.values()).toList() : dto.getStatus();
        Page<Product> products = productRepository.getFilteredProducts(q, status, page);

        return products.map(this::convertProductToProductDto);

    }
}
