package com.telusko.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.telusko.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Loads several products with their category in one query, skipping withdrawn ones.
     * <p>
     * Only ever used to turn ids chosen by an AI feature back into live rows, and those results go
     * straight to a customer, so the active check belongs in the query rather than in each caller.
     */
    @Query("select p from Product p left join fetch p.category where p.id in :ids and p.active = true")
    List<Product> findActiveByIdWithCategory(@Param("ids") List<Long> ids);

    // Restock questions ("what is running out?") are the most common thing an admin asks, and
    // stock is one of the fields that goes stale fastest in the vector store, so this reads
    // straight from the source of truth.
    @Query("select p from Product p where p.active = true and p.stockQty <= :threshold "
            + "order by p.stockQty asc")
    List<Product> findLowStock(@Param("threshold") int threshold, Pageable pageable);
}