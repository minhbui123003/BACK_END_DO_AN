package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Account;
import com.TTT.Tniciu_API.Model.AccountVoucher;
import com.TTT.Tniciu_API.Model.Voucher;
import com.TTT.Tniciu_API.Model.VoucherDTO;
import com.TTT.Tniciu_API.Repository.AccountRepository;
import com.TTT.Tniciu_API.Repository.AccountVoucherRepository;
import com.TTT.Tniciu_API.Repository.VoucherRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private AccountVoucherRepository accountVoucherRepository;

    @Autowired
    private AccountRepository accountRepository;

    // Lấy tất cả voucher
    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        // Cập nhật trạng thái của tất cả các voucher trước khi trả về
        vouchers.forEach(Voucher::updateStatus);
        voucherRepository.saveAll(vouchers); // Lưu lại các voucher đã cập nhật trạng thái
        return vouchers;
    }

    // Lấy voucher theo mã code
    public Optional<Voucher> getVoucherByCode(String code) {
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code);
        voucherOpt.ifPresent(Voucher::updateStatus); // Cập nhật trạng thái của voucher nếu cần
        return voucherOpt;
    }

    // Tạo voucher mới
    public Voucher createVoucher(Voucher voucher) {
        voucher.updateStatus(); // Đảm bảo trạng thái được cập nhật khi tạo mới
        return voucherRepository.save(voucher);
    }

    // Xóa voucher theo ID
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    public String applyVoucherToAccount(String accountId, String voucherCode) {
        // Tìm voucher theo mã
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(voucherCode);
        if (voucherOpt.isEmpty()) {
            return "Mã voucher không tồn tại!";
        }

        // Tìm tài khoản theo ID
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            return "Tài khoản không hợp lệ!";
        }

        Voucher voucher = voucherOpt.get();
        Account account = accountOpt.get();

        // Kiểm tra nếu voucher đã được lưu cho người dùng
        Optional<AccountVoucher> existingAccountVoucherOpt = account.getAccountVouchers().stream()
                .filter(av -> av.getVoucher().getCode().equals(voucherCode))
                .findFirst();

        if (existingAccountVoucherOpt.isPresent()) {
            // Nếu voucher đã được lưu, kiểm tra xem nó đã được áp dụng chưa
            AccountVoucher existingAccountVoucher = existingAccountVoucherOpt.get();
            if (existingAccountVoucher.isApplied()) {
                return "Mã voucher đã được áp dụng trước đó!";
            }

            // Nếu voucher đã được lưu nhưng chưa được áp dụng, cập nhật trạng thái áp dụng
            existingAccountVoucher.setApplied(false);
            accountVoucherRepository.save(existingAccountVoucher);

            return "Voucher đã được lưu và áp dụng thành công!";
        }

        // Lưu voucher cho tài khoản
        AccountVoucher accountVoucher = new AccountVoucher();
        accountVoucher.setAccount(account);
        accountVoucher.setVoucher(voucher);
        accountVoucher.setApplied(false); // Ban đầu voucher chưa được áp dụng
        accountVoucher.setSaved(true);    // Đánh dấu voucher đã được lưu
        accountVoucherRepository.save(accountVoucher);

        return "Voucher đã được lưu thành công!";
    }


    // Cập nhật voucher
    @Transactional
    public Optional<Voucher> updateVoucher(Long id, Voucher updatedVoucher) {
        Optional<Voucher> voucherOpt = voucherRepository.findById(id);

        if (voucherOpt.isPresent()) {
            Voucher existingVoucher = voucherOpt.get();
            // Cập nhật thông tin từ updatedVoucher
            existingVoucher.setCode(updatedVoucher.getCode());
            existingVoucher.setDiscount(updatedVoucher.getDiscount());
            existingVoucher.setExpirationDate(updatedVoucher.getExpirationDate());
            existingVoucher.setDescription(updatedVoucher.getDescription());
            existingVoucher.setActive(updatedVoucher.isActive()); // Đảm bảo cập nhật trạng thái

            // Cập nhật trạng thái voucher
            existingVoucher.updateStatus();

            // Lưu lại voucher đã cập nhật
            voucherRepository.save(existingVoucher);
            return Optional.of(existingVoucher);
        }

        return Optional.empty();
    }


    public boolean checkVoucherSaved(String accountId, String voucherCode) {
        Optional<AccountVoucher> accountVoucherOpt = accountVoucherRepository.findByAccountId(accountId).stream()
                .filter(av -> av.getVoucher().getCode().equals(voucherCode))
                .findFirst();

        return accountVoucherOpt.map(AccountVoucher::isSaved).orElse(false);
    }

    public List<VoucherDTO> getAccountVouchers(String accountId) {
        List<AccountVoucher> accountVouchers = accountVoucherRepository.findByAccountId(accountId);
        return accountVouchers.stream()
                .map(accountVoucher -> new VoucherDTO(
                       accountVoucher.getVoucher().getId(),
                        accountVoucher.getVoucher().getCode(),
                        accountVoucher.getVoucher().getDiscount()))
                .collect(Collectors.toList());
    }


}
