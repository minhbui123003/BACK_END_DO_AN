    package com.TTT.Tniciu_API.Controller;

    import com.TTT.Tniciu_API.Model.Account;
    import com.TTT.Tniciu_API.Service.AccountService;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.*;

    @RestController
    @RequestMapping("/api/account")
    public class AccountController {
        private final AccountService accountService;

        public AccountController(AccountService accountService) {
            this.accountService = accountService;
        }

        @PostMapping("/signup")
        public ResponseEntity<Map<String, Object>> signup(@RequestBody Account account) {
            try {
                accountService.signup(account);
                // Create a response map
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Signup successful");
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException ex) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }



        @PostMapping("/login")
        public ResponseEntity<Map<String, Object>> login(@RequestBody Account account) {
            try {
                Account authenticatedAccount = accountService.login(account);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Đăng nhập thành công");
                response.put("id", authenticatedAccount.getId());
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } catch (Exception e) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Đăng nhập thất bại. Vui lòng thử lại.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        }



        @PutMapping("update/{id}")
        public ResponseEntity<Account> updateAccount(@PathVariable String id, @RequestBody Account account) {
            Optional<Account> updatedAccount = accountService.updateAccountById(id, account);
            if (updatedAccount.isPresent()) {
                return ResponseEntity.ok(updatedAccount.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        @PutMapping("/update/internal/{id}")
        public ResponseEntity<Map<String, Object>> updateAccount(@PathVariable String id, @RequestBody Account account, @RequestHeader("requester-id") String requesterId) {
            Account requester = new Account();
            requester.setId(requesterId);

            try {
                Optional<Account> updatedAccount = accountService.updateAccountById(id, account, requester);
                if (updatedAccount.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Cập nhật tài khoản thành công");
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            } catch (IllegalArgumentException ex) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        @DeleteMapping("/delete/{id}")
        public ResponseEntity<Map<String, Object>> deleteAccount(@PathVariable String id, @RequestHeader("requester-id") String requesterId) {
            Account requester = new Account();
            requester.setId(requesterId);

            try {
                accountService.deleteAccountById(id, requester);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Xóa tài khoản thành công");
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException ex) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        @PostMapping("/create")
        public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Account account, @RequestHeader("requester-id") String requesterId) {
            Account requester = new Account();
            requester.setId(requesterId);

            try {
                Account createdAccount = accountService.createAccount(account, requester);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Tạo tài khoản thành công");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } catch (IllegalArgumentException ex) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        @GetMapping("/all")
        public ResponseEntity<List<Account>> getAllAccounts() {
            List<Account> accounts = accountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        }

        // New endpoint to get account by ID
        @GetMapping("/{id}")
        public ResponseEntity<Account> getAccountById(@PathVariable String id) {
            // Convert ID to a list to use the new method in AccountService
            List<String> ids = Arrays.asList(id);
            List<Account> accounts = accountService.getAccountsByIds(ids);
            // Check if the list is not empty and return the first account if it exists
            if (!accounts.isEmpty()) {
                return ResponseEntity.ok(accounts.get(0));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        @GetMapping("/customers")
        public ResponseEntity<List<Account>> getCustomers() {
            List<Account> customers = accountService.getCustomers();
            return ResponseEntity.ok(customers);
        }

        @GetMapping("/internal")
        public ResponseEntity<List<Account>> getInternalUsers() {
            List<Account> internalUsers = accountService.getInternalUsers();
            return ResponseEntity.ok(internalUsers);
        }
        @GetMapping("/count/customers")
        public long countTotalCustomers() {
            return accountService.countTotalCustomers();
        }
    }
