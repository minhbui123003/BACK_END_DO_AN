package com.TTT.Tniciu_API.Repository;

import com.TTT.Tniciu_API.Model.Account;
import com.TTT.Tniciu_API.Model.Role;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByVerificationToken(String token);
    Optional<Account> findByEmailAndVerificationToken(String email, String token); // Add this line

    List<Account> findAll();
    List<Account> findByIdIn(List<String> ids);
    List<Account> findByRole(Role role);

    @Query("SELECT a FROM Account a WHERE a.role.name IN :roleNames")
    List<Account> findByRoleNames(List<String> roleNames);
    long countByRole(Role role);
}
