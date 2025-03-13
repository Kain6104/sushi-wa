package com.mcnz.rps.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/vqr/api")
public class TokenController {

    private static final String VALID_USERNAME = "customer-vietqrtest-user2468";
    private static final String VALID_PASSWORD = "Y3VzdG9tZXItdmlldHFydGVzdC11c2VyMjQ2ODpZM1Z6ZEc5dFpYSXRkbWxsZEhGeWRHVnpkQzExYzJWeU1qUTJPQT09"; // Chuỗi base64 của username:password.

    @PostMapping("/token_generate")
    public ResponseEntity<?> generateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);

            String username = values[0];
            String password = values[1];

            if (VALID_USERNAME.equals(username) && VALID_PASSWORD.equals(password)) {
                String token = "your-generated-jwt-token"; // Thay bằng logic tạo JWT token thực tế.
                return ResponseEntity.ok(new TokenResponse(token, "Bearer", 300));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization header is missing or invalid");
        }
    }

    @PostMapping("/generate_qr")
    public ResponseEntity<?> generateQrCode(@RequestHeader("Authorization") String authHeader, @RequestBody QrRequest qrRequest) {
        ResponseEntity<?> tokenResponse = generateToken(authHeader);
        if (tokenResponse.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token generation failed.");
        }

        String token = ((TokenResponse) tokenResponse.getBody()).getAccess_token();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<QrRequest> request = new HttpEntity<>(qrRequest, headers);
        String vietqrApiUrl = "https://api.vietqr.vn/v2/generate-qr";

        try {
            ResponseEntity<QrResponse> response = restTemplate.postForEntity(vietqrApiUrl, request, QrResponse.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate QR Code.");
        }
    }

    public static class TokenResponse {
        private String access_token;
        private String token_type;
        private int expires_in;

        public TokenResponse(String access_token, String token_type, int expires_in) {
            this.access_token = access_token;
            this.token_type = token_type;
            this.expires_in = expires_in;
        }

        // Getters và Setters
        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public int getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(int expires_in) {
            this.expires_in = expires_in;
        }
    }

    public static class QrRequest {
        private String accountNo;
        private String bankCode;
        private String amount;
        private String content;

        // Getters & Setters
        public String getAccountNo() {
            return accountNo;
        }

        public void setAccountNo(String accountNo) {
            this.accountNo = accountNo;
        }

        public String getBankCode() {
            return bankCode;
        }

        public void setBankCode(String bankCode) {
            this.bankCode = bankCode;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class QrResponse {
        private String qrData;

        // Getters & Setters
        public String getQrData() {
            return qrData;
        }

        public void setQrData(String qrData) {
            this.qrData = qrData;
        }
    }
}
