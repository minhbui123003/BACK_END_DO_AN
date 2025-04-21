package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Account;
import com.TTT.Tniciu_API.Model.Order;
import com.TTT.Tniciu_API.Model.OrderDetail;
import com.TTT.Tniciu_API.Model.Product;
import com.TTT.Tniciu_API.Repository.AccountRepository;
import com.TTT.Tniciu_API.Repository.OrderRepository;
import com.TTT.Tniciu_API.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.net.URLEncoder;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    private Map<String, Order> temporaryOrders = new HashMap<>();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByAccountId(String accountId) {
        return orderRepository.findByAccountId(accountId);
    }

    public Order createOrder(Order order) {
        Optional<Account> accountOptional = accountRepository.findById(order.getAccount().getId());
        if (accountOptional.isPresent()) {
            // Kiểm tra số lượng hàng tồn kho trước khi tạo đơn
            checkProductQuantities(order);

            Account account = accountOptional.get();
            order.setAccount(account);
            order.setDate(LocalDateTime.now());

            // Xử lý trạng thái thanh toán dựa trên phương thức thanh toán
            if ("cod".equalsIgnoreCase(order.getPayment())) {
                order.setPaymentStatus("Chờ thanh toán");
            } else if ("bankTransfer".equalsIgnoreCase(order.getPayment())) {
                order.setPaymentStatus("Đã thanh toán");
            }

            // Liên kết sản phẩm với đơn hàng
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                productRepository.findById(orderDetail.getProduct().getId())
                        .ifPresentOrElse(product -> {
                            orderDetail.setOrder(order);
                            orderDetail.setProduct(product);
                        }, () -> {
                            throw new RuntimeException("Sản phẩm không tồn tại với ID: " + orderDetail.getProduct().getId());
                        });
            }

            // Lưu đơn hàng
            Order savedOrder = orderRepository.save(order);

            // Nếu trạng thái thanh toán là "Đã thanh toán", giảm số lượng sản phẩm
            if ("Đã thanh toán".equals(savedOrder.getPaymentStatus())) {
                updateProductQuantities(savedOrder);
            }

            return savedOrder;
        } else {
            throw new RuntimeException("Account not found with id: " + order.getAccount().getId());
        }
    }

    // Phương thức mới để kiểm tra số lượng hàng tồn kho
    public void checkProductQuantities(Order order) {
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            productRepository.findById(orderDetail.getProduct().getId())
                    .ifPresentOrElse(product -> {
                        // Kiểm tra số lượng sản phẩm
                        if (product.getQuantity() < orderDetail.getQuantity()) {
                            throw new RuntimeException("Không đủ số lượng sản phẩm: " + product.getName() +
                                    ". Số lượng còn lại: " + product.getQuantity());
                        }
                    }, () -> {
                        throw new RuntimeException("Sản phẩm không tồn tại với ID: " + orderDetail.getProduct().getId());
                    });
        }
    }

    // Phương thức cập nhật số lượng sản phẩm
    private void updateProductQuantities(Order order) {
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            productRepository.findById(orderDetail.getProduct().getId())
                    .ifPresentOrElse(product -> {
                        product.setQuantity(product.getQuantity() - orderDetail.getQuantity());
                        productRepository.save(product);
                    }, () -> {
                        throw new RuntimeException("Sản phẩm không tồn tại với ID: " + orderDetail.getProduct().getId());
                    });
        }
    }

    public void saveTemporaryOrder(Order order) {
        temporaryOrders.put(order.getVnpTxnRef(), order);
    }

    public Order getOrderByTxnRef(String vnpTxnRef) {
        return temporaryOrders.get(vnpTxnRef);
    }
    public Order updateOrder(Long id, Order orderDetails) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order existingOrder = order.get();
            String oldPaymentStatus = existingOrder.getPaymentStatus();

            existingOrder.setCustomerName(orderDetails.getCustomerName());
            existingOrder.setDate(orderDetails.getDate());
            existingOrder.setAddress(orderDetails.getAddress());
            existingOrder.setTotal(orderDetails.getTotal());
            existingOrder.setPayment(orderDetails.getPayment());
            existingOrder.setPaymentStatus(orderDetails.getPaymentStatus());
            existingOrder.setShippingStatus(orderDetails.getShippingStatus());
            existingOrder.setAccount(orderDetails.getAccount());

            // Tự động cập nhật paymentStatus thành "Đã thanh toán" khi shippingStatus là "Đã giao hàng"
            if ("Đã giao hàng".equals(orderDetails.getShippingStatus())) {
                existingOrder.setPaymentStatus("Đã thanh toán");
            }

            // Lưu đơn hàng
            Order savedOrder = orderRepository.save(existingOrder);

            // Nếu trạng thái thanh toán chuyển từ "Chờ thanh toán" sang "Đã thanh toán", giảm số lượng sản phẩm
            if (!"Đã thanh toán".equals(oldPaymentStatus) && "Đã thanh toán".equals(savedOrder.getPaymentStatus())) {
                updateProductQuantities(savedOrder);
            }

            return savedOrder;
        }
        return null;
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public double calculateTotalRevenue() {
        List<Order> orders = orderRepository.findAll();
        double totalRevenue = 0;

        for (Order order : orders) {
            // Chỉ tính doanh thu cho các đơn hàng đã thanh toán
            if (order.getPaymentStatus() != null && order.getPaymentStatus().equals("Đã thanh toán")) {
                totalRevenue += order.getTotal();
            }
        }

        return totalRevenue;
    }
    public long countTotalOrders() {
        return orderRepository.count();
    }

    public String createPaymentUrl(Order order) {
        try {
            // Kiểm tra số lượng sản phẩm trước khi tạo URL thanh toán
            checkProductQuantities(order);

            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
            String vnp_IpAddr = "127.0.0.1";
            String vnp_TmnCode = "TNICIUS1";
            String vnp_HashSecret = "TNICIUS1";
            String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
            String vnp_ReturnUrl = "http://localhost:3000/payment-success";
            String vnp_OrderInfo = "Thanh toan don hang: " + order.getId();
            String vnp_OrderType = "other";
            String vnp_Amount = String.valueOf((long) (order.getTotal() * 100));
            String vnp_Locale = "vn";
            String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String vnp_CurrCode = "VND";

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            List fieldNames = new ArrayList(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = (String) vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnp_Url + "?" + queryUrl;
            return paymentUrl;
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo URL thanh toán: " + e.getMessage());
        }
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), "HmacSHA512");
        sha512_HMAC.init(secret_key);
        return bytesToHex(sha512_HMAC.doFinal(data.getBytes(StandardCharsets.US_ASCII)));
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

