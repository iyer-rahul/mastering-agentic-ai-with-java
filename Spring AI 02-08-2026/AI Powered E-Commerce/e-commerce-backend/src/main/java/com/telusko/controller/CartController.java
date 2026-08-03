package com.telusko.controller;

import com.telusko.dto.CartResponseDto;
import com.telusko.dto.UpdateCartItemRequest;
import com.telusko.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ecommerce/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart operations")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getUserCart(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        CartResponseDto cart =
                cartService.getUserCart(currentUser.getUsername());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/item/{productId}")
    public ResponseEntity<CartResponseDto> addOrUpdateItem(
            @PathVariable Long productId,
            @RequestBody UpdateCartItemRequest req,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        CartResponseDto cart = cartService.addOrUpdateItem(
                currentUser.getUsername(),
                productId,
                req.getQuantity()
        );
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/item/{productId}")
    public ResponseEntity<CartResponseDto> removeItem(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        CartResponseDto cart = cartService.removeItem(
                currentUser.getUsername(), productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponseDto> clearCart(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        CartResponseDto cart = cartService.clearCart(
                currentUser.getUsername());
        return ResponseEntity.ok(cart);
    }
}
