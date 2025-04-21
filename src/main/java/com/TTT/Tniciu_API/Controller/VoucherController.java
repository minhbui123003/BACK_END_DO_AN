package com.TTT.Tniciu_API.Controller;

import com.TTT.Tniciu_API.Model.Voucher;
import com.TTT.Tniciu_API.Model.VoucherDTO;
import com.TTT.Tniciu_API.Service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public List<Voucher> getAllVouchers() {
        return voucherService.getAllVouchers();
    }

    @GetMapping("/{code}")
    public ResponseEntity<Voucher> getVoucherByCode(@PathVariable String code) {
        Optional<Voucher> voucher = voucherService.getVoucherByCode(code);
        return voucher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Voucher createVoucher(@RequestBody Voucher voucher) {
        return voucherService.createVoucher(voucher);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(
            @PathVariable Long id,
            @RequestBody Voucher updatedVoucher) {
        Optional<Voucher> updatedVoucherOpt = voucherService.updateVoucher(id, updatedVoucher);

        if (updatedVoucherOpt.isPresent()) {
            return ResponseEntity.ok(updatedVoucherOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/apply")
    public ResponseEntity<String> applyVoucher(@RequestParam String accountId, @RequestParam String voucherCode) {
        String response = voucherService.applyVoucherToAccount(accountId, voucherCode);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkVoucherSaved(
            @RequestParam String accountId,
            @RequestParam String voucherCode) {
        boolean response = voucherService.checkVoucherSaved(accountId, voucherCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<VoucherDTO>> getAccountVouchers(@PathVariable String accountId) {
        List<VoucherDTO> vouchers = voucherService.getAccountVouchers(accountId);
        return new ResponseEntity<>(vouchers, HttpStatus.OK);
    }
}
