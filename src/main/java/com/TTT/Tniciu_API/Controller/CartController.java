package com.TTT.Tniciu_API.Controller;

import com.TTT.Tniciu_API.Model.CartItem;
import com.TTT.Tniciu_API.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam String accountId
    ) {
        CartItem cartItem = cartService.addItemToCart(productId, quantity, accountId);
        return ResponseEntity.ok(cartItem);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<CartItem>> listCartItems(@PathVariable String accountId) {
        List<CartItem> cartItems = cartService.listCartItems(accountId);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity
    ) {
        CartItem updatedCartItem = cartService.updateCartItem(cartItemId, quantity);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/delete/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
}
