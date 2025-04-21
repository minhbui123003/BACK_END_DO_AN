package com.TTT.Tniciu_API.Repository;

import com.TTT.Tniciu_API.Model.AccountVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountVoucherRepository extends JpaRepository<AccountVoucher, Long> {
    List<AccountVoucher> findByAccountId(String accountId);
}
