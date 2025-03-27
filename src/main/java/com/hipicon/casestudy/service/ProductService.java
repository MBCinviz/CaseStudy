package com.hipicon.casestudy.service;

import com.hipicon.casestudy.dto.*;
import com.hipicon.casestudy.entity.Product;
import com.hipicon.casestudy.entity.User;
import com.hipicon.casestudy.exception.*;
import com.hipicon.casestudy.repository.ProductRepository;
import com.hipicon.casestudy.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Kullanıcı kimliği doğrulanmamış!");
            throw new UnauthorizedException("Kullanıcı kimliği doğrulanmamış!");

        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            log.warn("Geçersiz kullanıcı oturumu");
            throw new UnauthorizedException("Geçersiz kullanıcı oturumu");
        }

        UserDetails userDetails = (UserDetails) principal;

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userDetails.getUsername()));
    }

    @Transactional(rollbackOn = Exception.class)
    public ProductDto addProduct(ProductRequest productRequest) {
        User currentUser = getCurrentUser();

        if (productRequest.getName() == null || productRequest.getName().isBlank()) {
            log.warn("Ürün adı boş olamaz");
            throw new BadRequestException("Ürün adı boş olamaz");
        }
        if (productRequest.getSellerName() == null || productRequest.getSellerName().isBlank()) {
            log.warn("Satıcı adı boş olamaz");
            throw new BadRequestException("Satıcı adı boş olamaz");
        }
        if (productRequest.getDescription() == null || productRequest.getDescription().isBlank()) {
            log.warn("Açıklama alanı boş olamaz");
            throw new BadRequestException("Açıklama alanı boş olamaz");
        }
        if(productRequest.getDescription().length()>200){
            log.warn("Açıklama alanı 200 karakterden fazla olamaz");
            throw new BadRequestException("Açıklama alanı 200 karakterden fazla olamaz");
        }
        if (productRequest.getPrice() == null || productRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Ürün fiyatı sıfırdan büyük olmalıdır");
            throw new BadRequestException("Ürün fiyatı sıfırdan büyük olmalıdır");
        }
        if (productRequest.getStock() == null || productRequest.getStock() < 0) {
            log.warn("Ürün stok miktarı negatif olamaz");
            throw new BadRequestException("Ürün stok miktarı negatif olamaz");
        }
        if (productRequest.getImageUrls() == null || productRequest.getImageUrls().isEmpty()) {
            log.warn("Ürün resimleri olamaz");
            throw new BadRequestException("Ürün resimleri olamaz");
        }
        Product product = new Product();

        try {
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
        }catch (Exception e) {
            log.error("Ürün eklenirken bir hata oluştu: " + e.getMessage());
            throw new DatabaseOperationException("Ürün eklenirken bir hata oluştu: " + e.getMessage());
        }
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
    public ProductDto updateProductStatus(Long productId, ProductStatusRequest statusRequest) {
        User currentUser = getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Ürün Bulunamadı"));


        if (!product.getSeller().getId().equals(currentUser.getId())) {
            log.warn("Bu ürünü güncelleme yetkiniz yok");
            throw new UnauthorizedException("Bu ürünü güncelleme yetkiniz yok");
        }
        try {
            product.setStatus(statusRequest.getStatus());
             productRepository.save(product);
            return convertProductToProductDto(product);
        } catch (Exception e) {
            log.error("Ürün durumu güncellenirken bir hata oluştu: " + e.getMessage());
            throw new DatabaseOperationException("Ürün durumu güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    public List<ProductDto> getUserProducts() {
        User currentUser = getCurrentUser();

        List<Product> products = productRepository.findBySeller(currentUser);


        if (products.isEmpty()) {
            log.warn("Bu kullanıcıya ait ürün bulunamadı.");
            throw new ProductNotFoundException("Bu kullanıcıya ait ürün bulunamadı.");
        }
        return products.stream()
                .map(this::convertProductToProductDto)
                .collect(Collectors.toList());

    }

    public DashBoardResponse getUserDashboard() {
        User currentUser = getCurrentUser();

        Long totalProducts = productRepository.countBySeller(currentUser);
        Integer totalStock = productRepository.getTotalStockBySeller(currentUser);
        List<Product>  products= productRepository.findBySellerAndStockLessThan(currentUser, 1);

        List<ProductDto> lowStockProducts=products.stream()
                .map(this::convertProductToProductDto)
                .collect(Collectors.toList());
        if (totalStock == null) {
            totalStock = 0;
        }
        if (totalProducts == 0) {
            log.warn("Bu kullanıcıya ait ürün bulunamadı.");
            throw new ProductNotFoundException("Bu kullanıcıya ait ürün bulunamadı.");
        }


        return new DashBoardResponse(totalProducts, totalStock, lowStockProducts);
    }

    public List<ProductDto> getSameNameProducts(String name) {
        User currentUser = getCurrentUser();
        List<Product> products = productRepository.getSameNameProducts(name);
        if (products.isEmpty()) {
            log.warn("Bu isime ait ürün bulunamadı: " + name);
            throw new ProductNotFoundException("Bu isime ait ürün bulunamadı: " + name);
        }
        return products.stream()
                .map(this::convertProductToProductDto)
                .collect(Collectors.toList());
    }

    public Page<ProductDto> getFilteredProducts(ProductFilterDto dto) {
        User currentUser = getCurrentUser();
        Integer pageNumber = Objects.requireNonNullElse(dto.getPage(), 0);
        Pageable page = PageRequest.of(pageNumber, 3);
        String q = StringUtils.isEmpty(dto.getSearchTerm()) ? null : dto.getSearchTerm().toLowerCase();
        List<Product.ProductStatus> status = CollectionUtils.isEmpty(dto.getStatus()) ? Arrays.stream(Product.ProductStatus.values()).toList() : dto.getStatus();
        Page<Product> products = productRepository.getFilteredProducts(q, status, page);

        if (products.isEmpty()) {
            log.warn("Filtreye uygun ürün bulunamadı.");
            throw new ProductNotFoundException("Filtreye uygun ürün bulunamadı.");
        }
        return products.map(this::convertProductToProductDto);

    }
}
