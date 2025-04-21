package com.TTT.Tniciu_API.Repository;

import com.TTT.Tniciu_API.Model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);  // Tìm voucher theo mã
}
