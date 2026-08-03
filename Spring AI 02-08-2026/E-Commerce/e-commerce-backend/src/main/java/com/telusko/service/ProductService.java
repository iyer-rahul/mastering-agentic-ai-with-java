package com.telusko.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.telusko.dto.CreateProductRequest;
import com.telusko.dto.ProductResponseDto;
import com.telusko.dto.UpdateProductRequest;
import com.telusko.model.Category;
import com.telusko.model.Product;
import com.telusko.repository.CategoryRepository;
import com.telusko.repository.ProductRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final CloudinaryService cloudinary;


    public Page<ProductResponseDto> getAllProducts(int page, int limit) {

        if (page < 1) page = 1;
        if (limit < 1) limit = 10;

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "name"));

        return products.findByActiveTrue(pageable)
                .map(this::toDto);
    }

    // Public so any other feature that needs to return products uses the same shape the REST
    // endpoints do, instead of maintaining a second mapping that could drift.
    public ProductResponseDto toDto(Product p) {
        Category c = p.getCategory();

        return ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQty(p.getStockQty())
                .sku(p.getSku())
                .active(p.getActive())
                .mainImage(p.getMainImage())
                .subImages(p.getSubImages())
                .categoryId(c != null ? c.getId() : null)
                .categoryName(c != null ? c.getName() : null)
                .build();
    }


    public ProductResponseDto createProduct(CreateProductRequest req,
                                            MultipartFile mainImage,
                                            List<MultipartFile> subImages) {

        Category category = categories.findById(req.getCategory())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found with id: " + req.getCategory()));

        String mainImageUrl = cloudinary.uploadToCloudinary(mainImage, "products/main");

        List<String> subImageUrls = new ArrayList<>();
        if (subImages != null) {
            for (int i = 0; i < subImages.size() && i < 4; i++) {
                MultipartFile file = subImages.get(i);
                if (!file.isEmpty()) {
                    subImageUrls.add(cloudinary.uploadToCloudinary(file, "products/sub"));
                }
            }
        }

        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stockQty(req.getStock())
                .sku("SKU-" + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase())
                .active(true)
                .mainImage(mainImageUrl)
                .subImages(subImageUrls)
                .category(category)
                .build();

        Product saved = products.save(product);

        return toDto(saved);
    }

    public ProductResponseDto getProductById(Long productId) {
        Product product = products.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + productId));
        return toDto(product);
    }

    public void deleteProduct(Long productId) {
        Product product = products.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + productId));
        product.setActive(false);
        products.save(product);
    }

    public ProductResponseDto updateProduct(Long productId,
                                            UpdateProductRequest req,
                                            MultipartFile mainImage,
                                            List<MultipartFile> subImages) {

        Product product = products.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + productId));

        if (req.getName() != null && !req.getName().isBlank()) {
            product.setName(req.getName());
        }
        if (req.getDescription() != null) {
            product.setDescription(req.getDescription());
        }
        if (req.getPrice() != null) {
            product.setPrice(req.getPrice());
        }
        if (req.getStock() != null) {
            product.setStockQty(req.getStock());
        }
        if (req.getActive() != null) {
            product.setActive(req.getActive());
        }
        if (req.getCategory() != null) {
            Category category = categories.findById(req.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Category not found with id: " + req.getCategory()));
            product.setCategory(category);
        }

        if (mainImage != null && !mainImage.isEmpty()) {
            String mainUrl = cloudinary.uploadToCloudinary(mainImage, "products/main");
            product.setMainImage(mainUrl);
        }

        if (subImages != null && !subImages.isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < subImages.size() && i < 4; i++) {
                MultipartFile file = subImages.get(i);
                if (!file.isEmpty()) {
                    urls.add(cloudinary.uploadToCloudinary(file, "products/sub"));
                }
            }
            if (!urls.isEmpty()) {
                product.setSubImages(urls);
            }
        }

        Product saved = products.save(product);


        return toDto(saved);
    }

    public Page<ProductResponseDto> getProductsByCategory(Long categoryId,
                                                          int page,
                                                          int limit) {
        if (page < 1) page = 1;
        if (limit < 1) limit = 5;

        categories.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + categoryId));

        Pageable pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Direction.ASC, "name"));

        return products.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::toDto);
    }

    /**
     * Keyword search across the catalog.
     * <p>
     * A plain SQL match on name, description and category name, case insensitive. It finds what the
     * shopper literally typed: "mouse" finds the Mouse, and "something for my desk" finds nothing,
     * because no product contains those words. That limitation is the whole reason semantic search
     * exists, and this is the method it replaces.
     */
    public List<ProductResponseDto> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return products.searchActive(query.trim()).stream()
                .map(this::toDto)
                .toList();
    }

}
