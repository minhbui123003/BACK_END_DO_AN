package com.TTT.Tniciu_API.Repository;

import com.TTT.Tniciu_API.Model.CartItem;
import com.TTT.Tniciu_API.Model.Account;
import com.TTT.Tniciu_API.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByAccount(Account account);

    Optional<CartItem> findByProductAndAccount(Product product, Account account);
}
