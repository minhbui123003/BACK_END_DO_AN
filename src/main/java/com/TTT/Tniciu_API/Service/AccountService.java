package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Model.Account;
import com.TTT.Tniciu_API.Model.Role;
import com.TTT.Tniciu_API.Repository.AccountRepository;
import com.TTT.Tniciu_API.Repository.RoleRepository;
import com.TTT.Tniciu_API.Utils.PGPUtils;
import com.TTT.Tniciu_API.Utils.PasswordHasher;
import jakarta.mail.MessagingException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PGPPublicKey pgpPublicKey;
    private final EmailService emailService;
    private final PGPPrivateKey pgpPrivateKey;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;


        try {
            PGPKeyPair pgpKeyPair = PGPUtils.generateKeyPair();
            this.pgpPublicKey = pgpKeyPair.getPublicKey();
            this.pgpPrivateKey = pgpKeyPair.getPrivateKey();
        } catch (PGPException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize PGP keys");
        }

    }

    public void signup(Account accountDTO) {
        if (accountRepository.findByEmail(accountDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Account account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());

        // Sử dụng PasswordHasher để băm mật khẩu
        String hashedPassword = PasswordHasher.hashPassword(accountDTO.getPassword());
        account.setPassword(hashedPassword);

        if (accountDTO.getRole() != null && accountDTO.getRole().getId() != null) {
            Role role = roleRepository.findById(accountDTO.getRole().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vai trò không tìm thấy"));
            account.setRole(role);
        } else {
            Role defaultRole = roleRepository.findByName("Khách Hàng")
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò mặc định"));
            account.setRole(defaultRole);
        }

        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        // Tạo token xác thực và gửi email
        String verificationToken = UUID.randomUUID().toString();
        account.setVerificationToken(verificationToken);
        accountRepository.save(account);

        try {
            emailService.sendVerificationEmail(account.getEmail(), verificationToken, account.getEmail());
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể gửi email xác thực");
        }
    }


    public boolean verifyAccountByTokenAndEmail(String token, String email) {
        Optional<Account> accountOptional = accountRepository.findByEmailAndVerificationToken(email, token);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setVerificationToken(null); // Clear the verification token
            account.setEnabled(true); // Enable the account
            accountRepository.save(account);
            return true;
        } else {
            return false;
        }
    }

    public Account login(Account accountDTO) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(accountDTO.getEmail());
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();

            // Kiểm tra nếu tài khoản chưa được kích hoạt
            if (!account.isEnabled()) {
                throw new IllegalArgumentException("Tài khoản chưa được xác nhận. Vui lòng kiểm tra email của bạn.");
            }

            // Kiểm tra mật khẩu sử dụng PasswordHasher
            if (PasswordHasher.verifyPassword(accountDTO.getPassword(), account.getPassword())) {
                return account;
            } else {
                throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
            }
        }
        throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
    }

    public boolean verifyAccount(String email, String encryptedVerificationCode) {
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        if (optionalAccount.isPresent()) {
            try {
                String decryptedVerificationCode = PGPUtils.decryptVerificationCode(encryptedVerificationCode, pgpPrivateKey);
                // Implement logic to compare decryptedVerificationCode with the code sent via email
                return true;

            } catch (PGPException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Optional<Account> updateAccountById(String  id, Account updatedAccount) {
        return accountRepository.findById(id).map(existingAccount -> {
            // Update account information...
            existingAccount.setUsername(updatedAccount.getUsername());
            existingAccount.setEmail(updatedAccount.getEmail());

            if (updatedAccount.getPassword() != null && !updatedAccount.getPassword().isEmpty()) {
                String hashedPassword = PasswordHasher.hashPassword(updatedAccount.getPassword());
                existingAccount.setPassword(hashedPassword);
            }

            if (updatedAccount.getRole() != null && updatedAccount.getRole().getName() != null) {
                Role role = roleRepository.findByName(updatedAccount.getRole().getName())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found"));
                existingAccount.setRole(role);
            }

            // Update image if provided
            if (updatedAccount.getImage() != null && !updatedAccount.getImage().isEmpty()) {
                try {
                    String imageUrlString = updatedAccount.getImage();
                    File imageFile;

                    if (imageUrlString.startsWith("data:")) {
                        // Handle data URL
                        String base64Image = extractBase64String(imageUrlString);
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                        // Create temporary file
                        imageFile = File.createTempFile("image", ".tmp");
                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            fos.write(imageBytes);
                        }
                    } else {
                        // Handle normal URL
                        URL imageUrl = new URL(imageUrlString);
                        imageFile = File.createTempFile("image", ".tmp");

                        try (InputStream in = imageUrl.openStream()) {
                            Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    // Upload image to Cloudinary
                    String newImageUrl = cloudinaryService.uploadImage(imageFile);

                    // Delete temporary file
                    imageFile.delete();

                    // Update image URL in the database
                    existingAccount.setImage(newImageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle exception if any error occurs during image upload
                }
            }

            return accountRepository.save(existingAccount);
        });
    }
    public Optional<Account> updateAccountById(String id, Account updatedAccount, Account requester) {
        Account requesterAccount = accountRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản yêu cầu"));

        // Kiểm tra quyền của người yêu cầu
        if (!canPerformAction(requesterAccount, updatedAccount)) {
            throw new IllegalArgumentException("Hành động không được phép");
        }

        return accountRepository.findById(id).map(existingAccount -> {
            // Quản lý chỉ có thể cập nhật thông tin của khách hàng
            if (requesterAccount.getRole().getName().equals("Quản Lý") &&
                    !existingAccount.getRole().getName().equals("Khách Hàng")) {
                throw new IllegalArgumentException("Quản lý chỉ được phép cập nhật thông tin của khách hàng");
            }

            // Update account information...
            existingAccount.setUsername(updatedAccount.getUsername());
            existingAccount.setEmail(updatedAccount.getEmail());

            if (updatedAccount.getPassword() != null && !updatedAccount.getPassword().isEmpty()) {
                String hashedPassword = PasswordHasher.hashPassword(updatedAccount.getPassword());
                existingAccount.setPassword(hashedPassword);
            }

            if (updatedAccount.getRole() != null && updatedAccount.getRole().getName() != null) {
                Role role = roleRepository.findByName(updatedAccount.getRole().getName())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò"));
                existingAccount.setRole(role);
            }

            // Update image if provided
            if (updatedAccount.getImage() != null && !updatedAccount.getImage().isEmpty()) {
                try {
                    String imageUrlString = updatedAccount.getImage();
                    File imageFile;

                    if (imageUrlString.startsWith("data:")) {
                        // Handle data URL
                        String base64Image = extractBase64String(imageUrlString);
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                        // Create temporary file
                        imageFile = File.createTempFile("image", ".tmp");
                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            fos.write(imageBytes);
                        }
                    } else {
                        // Handle normal URL
                        URL imageUrl = new URL(imageUrlString);
                        imageFile = File.createTempFile("image", ".tmp");

                        try (InputStream in = imageUrl.openStream()) {
                            Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    // Upload image to Cloudinary
                    String newImageUrl = cloudinaryService.uploadImage(imageFile);

                    // Delete temporary file
                    imageFile.delete();

                    // Update image URL in the database
                    existingAccount.setImage(newImageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle exception if any error occurs during image upload
                }
            }

            return accountRepository.save(existingAccount);
        });
    }


    public void deleteAccountById(String id, Account requester) {
        Account requesterAccount = accountRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản yêu cầu"));

        Account accountToDelete = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cần xóa"));

        if (!canPerformAction(requesterAccount, accountToDelete)) {
            throw new IllegalArgumentException("Hành động không được phép");
        }

        accountRepository.deleteById(id);
    }

    public Account createAccount(Account newAccount, Account requester) {
        Account requesterAccount = accountRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản yêu cầu"));

        if (!canPerformAction(requesterAccount, newAccount)) {
            throw new IllegalArgumentException("Hành động không được phép");
        }
        if (newAccount.getImage() != null && !newAccount.getImage().isEmpty()) {
            try {
                String imageUrlString = newAccount.getImage();
                File imageFile;

                if (imageUrlString.startsWith("data:")) {
                    // Handle data URL
                    String base64Image = extractBase64String(imageUrlString);
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    // Create temporary file
                    imageFile = File.createTempFile("image", ".tmp");
                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                        fos.write(imageBytes);
                    }
                } else {
                    // Handle normal URL
                    URL imageUrl = new URL(imageUrlString);
                    imageFile = File.createTempFile("image", ".tmp");

                    try (InputStream in = imageUrl.openStream()) {
                        Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // Upload image to Cloudinary
                String newImageUrl = cloudinaryService.uploadImage(imageFile);

                // Delete temporary file
                imageFile.delete();

                // Update image URL in the database
                newAccount.setImage(newImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle exception if any error occurs during image upload
            }
        }
        newAccount.setId(UUID.randomUUID().toString());
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setPassword(PasswordHasher.hashPassword(newAccount.getPassword()));

        return accountRepository.save(newAccount);
    }

    private boolean canPerformAction(Account requester, Account targetAccount) {
        if (requester.getRole().getName().equals("Quản Trị Viên")) {
            return true;
        } else if (requester.getRole().getName().equals("Quản Lý")) {
            return targetAccount.getRole().getName().equals("Khách Hàng");
        } else {
            return false;
        }
    }
    // Method to extract base64 string from data URL
    private String extractBase64String(String dataUrl) {
        String base64Image = dataUrl.split(",")[1];
        return base64Image;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> getAccountsByIds(List<String> id) {
        return accountRepository.findByIdIn(id);
    }
    public List<Account> getCustomers() {
        Role customerRole = roleRepository.findByName("Khách Hàng")
                .orElseThrow(() -> new IllegalArgumentException("Customer role not found"));

        return accountRepository.findByRole(customerRole);
    }

    public List<Account> getInternalUsers() {
        List<String> internalRoles = Arrays.asList("Quản Lý", "Quản Trị Viên");
        return accountRepository.findByRoleNames(internalRoles);
    }
    public long countTotalCustomers() {
        Role customerRole = roleRepository.findByName("Khách Hàng")
                .orElseThrow(() -> new IllegalArgumentException("Vai trò khách hàng không tìm thấy"));
        return accountRepository.countByRole(customerRole);
    }
}