package com.TTT.Tniciu_API.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã giảm giá là bắt buộc")
    @Column(unique = true)
    private String code;

    @NotNull(message = "Giá trị giảm giá là bắt buộc")
    private double discount;

    private LocalDate expirationDate;

    @Column(length = 255)
    private String description;

    private boolean isActive = true;  // Để quản lý trạng thái kích hoạt của mã

    // Tính xem mã có còn hiệu lực hay không
    public boolean isValid() {
        // Kiểm tra trạng thái kích hoạt và ngày hết hạn
        return isActive && (expirationDate == null || LocalDate.now().isBefore(expirationDate));
    }

    public void updateStatus() {
        if (expirationDate != null && LocalDate.now().isAfter(expirationDate)) {
            this.isActive = false;  // Vô hiệu hóa voucher nếu ngày hết hạn đã qua
        } else {
            this.isActive = true;   // Kích hoạt voucher nếu ngày hiện tại trước ngày hết hạn
        }
    }

}
