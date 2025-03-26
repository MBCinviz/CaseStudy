package com.hipicon.casestudy.repository;

import com.hipicon.casestudy.entity.Product;
import com.hipicon.casestudy.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller(User seller);

    List<Product> findBySellerAndStatusIn(User seller, List<Product.ProductStatus> status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller= ?1")
    Long countBySeller(User seller);

    @Query("SELECT SUM(p.stock) FROM Product p WHERE p.seller=?1")
    Integer getTotalStockBySeller(User seller);

    List<Product> findBySellerAndStockLessThan(User seller, Integer stock);

    @Query("SELECT p FROM Product p WHERE p.name= :name")
    List<Product> getSameNameProducts(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.status IN (:status) AND (:searchTerm IS NULL OR lower(p.name) LIKE %:searchTerm%)")
    Page<Product> getFilteredProducts(@Param("searchTerm") String searchTerm, @Param("status") List<Product.ProductStatus> status, Pageable pageable);



}
