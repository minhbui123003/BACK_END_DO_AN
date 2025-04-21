package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Account;
import com.TTT.Tniciu_API.Model.CartItem;
import com.TTT.Tniciu_API.Model.Product;
import com.TTT.Tniciu_API.Repository.AccountRepository;
import com.TTT.Tniciu_API.Repository.CartItemRepository;
import com.TTT.Tniciu_API.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    public CartItem addItemToCart(Long productId, int quantity, String accountId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (productOpt.isPresent() && accountOpt.isPresent()) {
            Product product = productOpt.get();
            Account account = accountOpt.get();

            // Kiểm tra xem sản phẩm đã có trong giỏ hàng của tài khoản này chưa
            Optional<CartItem> existingCartItemOpt = cartItemRepository.findByProductAndAccount(product, account);

            if (existingCartItemOpt.isPresent()) {
                // Nếu đã có sản phẩm này trong giỏ hàng, tăng số lượng lên
                CartItem existingCartItem = existingCartItemOpt.get();
                existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
                return cartItemRepository.save(existingCartItem);
            } else {
                // Nếu chưa có, thêm mới vào giỏ hàng
                CartItem cartItem = new CartItem(product, account, quantity);
                return cartItemRepository.save(cartItem);
            }
        } else {
            throw new RuntimeException("Product or Account not found.");
        }
    }

    // Các phương thức listCartItems, updateCartItem, removeCartItem không thay đổi
    // Vẫn giữ nguyên như bạn đã cài đặt

    public List<CartItem> listCartItems(String accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            return cartItemRepository.findByAccount(account);
        } else {
            throw new RuntimeException("Account not found with id: " + accountId);
        }
    }

    public CartItem updateCartItem(Long cartItemId, int quantity) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            cartItem.setQuantity(quantity);
            return cartItemRepository.save(cartItem);
        } else {
            throw new RuntimeException("Cart item not found with id: " + cartItemId);
        }
    }

    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
