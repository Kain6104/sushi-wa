package com.mcnz.rps.controller;

import com.mcnz.rps.model.User;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.service.OrderService;
import com.mcnz.rps.service.EmailService;
import com.mcnz.rps.service.NotificationService;
import com.mcnz.rps.service.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

import com.mcnz.rps.model.Notification;
import com.mcnz.rps.model.OrderDetail;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private EmailService emailService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/")
    public String redirectToIndex(Model model,HttpSession session) {
        model.addAttribute("page", "index");
        // Lấy thông tin người dùng từ session
        String username = (String) session.getAttribute("loggedInUser");

        // Lấy thông báo cho người dùng hiện tại
        List<Notification> notifications = notificationService.getUserNotifications(username);
        long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();

        // Thêm thông báo và số lượng chưa đọc vào model
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationsCount", unreadCount);
        return "redirect:/index";
    }
    

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // Kiểm tra nếu người dùng đã đăng nhập
        if (session.getAttribute("loggedInUser") != null) {
            // Nếu đã đăng nhập, chuyển hướng tới trang chủ hoặc trang người dùng
            return "redirect:/index"; // Hoặc trang bất kỳ mà bạn muốn
        }
        return "login"; // Nếu chưa đăng nhập, hiển thị trang đăng nhập
    }

    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789";

    @GetMapping("/generate-captcha")
    @ResponseBody
    public String generateCaptcha(HttpSession session) {
        SecureRandom random = new SecureRandom();
        StringBuilder captcha = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            captcha.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }

        String captchaCode = captcha.toString();
        session.setAttribute("captchaCode", captchaCode); // Lưu vào session
        return captchaCode; // Trả về mã CAPTCHA cho client
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(value = "captchaInput", required = false) String captchaInput, // Nhận CAPTCHA từ form
            @RequestParam(value = "redirectURL", required = false) String redirectURL,
            HttpSession session,
            Model model) {


    	// Lấy số lần nhập sai từ session, mặc định là 0
        Integer failedAttempts = (Integer) session.getAttribute("failedAttempts");
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
        
        Long lockTime = (Long) session.getAttribute("lockTime");

        if (lockTime != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lockTime < 60000) {
                model.addAttribute("error", "Bạn đã nhập sai quá nhiều lần. Vui lòng bấm <a href='/forgot-password'>quên mật khẩu</a> hoặc thử lại sau 1 phút.");
                model.addAttribute("showCaptcha", true);
                return "login";
            } else {
                session.removeAttribute("lockTime");
                session.setAttribute("failedAttempts", 0);
                failedAttempts = 0;
            }
        }
        // Tìm kiếm người dùng bằng username hoặc email
        User user = userService.findByUsernameOrEmail(username);


     // Kiểm tra người dùng tồn tại và mật khẩu đúng
        if (user == null || !userService.authenticate(user.getUsername(), password)) {
            failedAttempts++; // Tăng số lần nhập sai
            session.setAttribute("failedAttempts", failedAttempts);


            if (failedAttempts >= 10) {
                session.setAttribute("lockTime", System.currentTimeMillis());
                model.addAttribute("error", "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 1 phút.");
                return "login";
            }

            if (failedAttempts >= 4) {
                String sessionCaptcha = (String) session.getAttribute("captchaCode");


                if (captchaInput == null || captchaInput.trim().isEmpty()) {
                    model.addAttribute("errorCaptcha", "Bạn đã nhập sai tài khoản/mật khẩu quá nhiều lần. Vui lòng nhập mã CAPTCHA.");
                    model.addAttribute("showCaptcha", true); // Giữ CAPTCHA hiển thị
                    return "login";
                }
                if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captchaInput)) {
                    model.addAttribute("error", "Mã CAPTCHA không chính xác.");
                    model.addAttribute("showCaptcha", true); // Giữ CAPTCHA hiển thị
                    return "login";
                }
                
                // Xóa CAPTCHA sau khi kiểm tra thành công để tránh dùng lại
                session.removeAttribute("captchaCode");
            }

            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
            return "login";
        }
        // Nếu sai >= 3 lần, kiểm tra CAPTCHA
        if (failedAttempts >= 3) {
            String sessionCaptcha = (String) session.getAttribute("captchaCode");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captchaInput)) {
                model.addAttribute("error", "Mã CAPTCHA không chính xác.");
                model.addAttribute("showCaptcha", true); // Giữ CAPTCHA hiển thị
                return "login";
            }
        }

        // Reset số lần nhập sai sau khi đăng nhập thành công
        session.removeAttribute("failedAttempts");
        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
            // Reset trạng thái buộc đăng nhập lại
            user.setForceRelogin(false);
            userService.updateUser(user);
            return "error";
        }

        if (user.isAccountLocked()) {
            model.addAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            return "login";
        }

        // Kiểm tra email đã xác thực hay chưa
        if (!user.isEmailVerified()) {
            // Gửi lại email xác thực
            String newToken = UUID.randomUUID().toString();
            user.setVerificationToken(newToken);
            userService.updateUser(user);


            String verificationLink = "http://localhost:8080/verify-email?token=" + newToken;
            String emailContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <style>" +
                    "        body {" +
                    "            font-family: Arial, sans-serif;" +
                    "            line-height: 1.6;" +
                    "            color: #333;" +
                    "            text-align: center;" +
                    "            background-color: #f9f9f9;" +
                    "            padding: 20px;" +
                    "        }" +
                    "        .email-container {" +
                    "            max-width: 600px;" +
                    "            margin: 0 auto;" +
                    "            background: #fff;" +
                    "            border-radius: 10px;" +
                    "            overflow: hidden;" +
                    "            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);" +
                    "        }" +
                    "        .header {" +
                    "            background-color: #4CAF50;" +
                    "            color: #fff;" +
                    "            padding: 20px;" +
                    "            font-size: 24px;" +
                    "        }" +
                    "        .content {" +
                    "            padding: 20px;" +
                    "        }" +
                    "        .content p {" +
                    "            margin: 10px 0;" +
                    "        }" +
                    "        .button {" +
                    "            display: inline-block;" +
                    "            margin-top: 20px;" +
                    "            padding: 10px 20px;" +
                    "            background-color: #4CAF50;" +
                    "            color: #fff;" +
                    "            text-decoration: none;" +
                    "            border-radius: 5px;" +
                    "            font-size: 16px;" +
                    "        }" +
                    "        .footer {" +
                    "            background-color: #f1f1f1;" +
                    "            padding: 10px;" +
                    "            font-size: 12px;" +
                    "            color: #666;" +
                    "        }" +
                    "        .social-icons img {" +
                    "            width: 30px;" +
                    "            margin: 0 5px;" +
                    "        }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='email-container'>" +
                    "        <div class='header'>" +
                    "            <img src='https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa.jpg' alt='Website Logo' style='max-width: 150px;'>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <p>Chào " + username + ",</p>" +
                    "            <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Website của chúng tôi</strong>.</p>" +
                    "            <p>Vui lòng nhấn vào liên kết bên dưới để xác nhận email và kích hoạt tài khoản của bạn:</p>" +
                    "            <a href='" + verificationLink + "' class='button'>Xác nhận Email</a>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>Theo dõi chúng tôi trên:</p>" +
                    "            <div class='social-icons'>" +
                    "                <a href='https://facebook.com'><img src='https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/2023_Facebook_icon.svg/2048px-2023_Facebook_icon.svg.png' alt='Facebook'></a>" +
                    "                <a href='https://tiktok.com'><img src='https://freepnglogo.com/images/all_img/1691751088logo-tiktok-png.png' alt='TikTok'></a>" +
                    "                <a href='https://instagram.com'><img src='https://cdn-icons-png.flaticon.com/512/2111/2111463.png' alt='Instagram'></a>" +
                    "            </div>" +
                    "            <p>&copy; 2024 Website của chúng tôi. All rights reserved.</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";


            emailService.sendEmail(user.getEmail(), "Xác nhận Email", emailContent);
            String email = user.getEmail();
            String maskedEmail = maskEmail(email);

            model.addAttribute("error", "Tài khoản chưa được xác thực. Liên kết xác thực đã được gửi vào " + maskedEmail + " của bạn.");
            return "login";
        }

        // Đăng nhập thành công
        // sessionManager.loginUser(username);
        // Đăng nhập thành công, lưu thông tin phiên vào cơ sở dữ liệu
        userService.loginUser(username, session);


        // Lưu thông tin vào session
        session.setAttribute("userId", user.getCustomerId());
        session.setAttribute("loggedInUser", user.getUsername());
        session.setAttribute("role", user.getRole());

        // Chuyển hướng về trang admin nếu là admin
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin";
        }

        // Chuyển về trang trước đó nếu có
        if (redirectURL != null && !redirectURL.isEmpty()) {
            return "redirect:" + redirectURL;
        }

        // Mặc định chuyển về trang index nếu không có redirectURL
        return "redirect:/index";
    }
    

    @PostMapping("/login/google")
    @ResponseBody
    public Map<String, Object> processGoogleLogin(@RequestBody Map<String, String> userData, HttpSession session) {
        String email = userData.get("email");
        String username = generateUsernameFromEmail(email); // Lấy username từ email
        String name = userData.get("username");

        // Kiểm tra nếu người dùng đã tồn tại trong hệ thống
        User user = userService.findByUsernameOrEmail(username);
        if (user == null) {
            // Nếu chưa tồn tại, tạo tài khoản mới
            String defaultPassword = "SSWA123";
            String hashedPassword = hashPassword(defaultPassword); // Hash mật khẩu mặc định

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setName(name);  // Đặt tên người dùng từ Google
            newUser.setRole("user");  // Gán vai trò người dùng
            newUser.setPasswordHash(hashedPassword);  // Lưu mật khẩu đã được hash
            newUser.setEmailVerified(true);  // Đánh dấu email đã xác thực
            newUser.setCreatedAt(LocalDateTime.now());  // Lưu thời gian tạo tài khoản
            userService.saveUser(newUser);  // Lưu người dùng mới vào database
            user = newUser;  // Gán người dùng mới vào đối tượng user
        }

        // Kiểm tra tài khoản có bị khóa không
        if (user.isAccountLocked()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            return response;
        }

        // Định dạng thời gian tạo tài khoản (chuyển thành String)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedCreatedAt = user.getCreatedAt().format(formatter);

        // Lưu thông tin người dùng vào session
        session.setAttribute("userId", user.getCustomerId());
        session.setAttribute("loggedInUser", user.getUsername());
        session.setAttribute("role", user.getRole());
        session.setAttribute("createdAt", formattedCreatedAt);  // Lưu thời gian vào session
        user.setForceRelogin(false);
        userService.updateUser(user);

        // Trả về phản hồi thành công
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    // Hàm tạo username từ email, lấy phần tên trước dấu "@" và loại bỏ dấu
    private String generateUsernameFromEmail(String email) {
        String username = email.split("@")[0];  // Lấy phần trước "@" từ email
        return removeVietnameseAccents(username);  // Loại bỏ dấu tiếng Việt
    }

    // Hàm loại bỏ dấu tiếng Việt
    private String removeVietnameseAccents(String str) {
        if (str == null) return null;
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    // Hàm hash mật khẩu
    private String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
    @GetMapping("/logout")
    public String processLogout(HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username != null) {
            sessionManager.logoutUser(username);
        }
        session.invalidate();
        return "redirect:/login";
    }



 // Phương thức giúp ẩn email theo yêu cầu
    private String maskEmail(String email) {
        // Tìm vị trí dấu '@'
        int atIndex = email.indexOf('@');
        if (atIndex != -1 && atIndex > 5) {
            String prefix = email.substring(0, 3);  // 3 chữ đầu
            String suffix = email.substring(atIndex - 2, atIndex);  // 2 chữ cuối trước '@'
            String domain = email.substring(atIndex);  // Phần đuôi domain

            return prefix + "***" + suffix + domain;
        }
        return email;  // Nếu không có phần chữ đủ để che thì trả về email gốc
    }
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Tên của template cho trang đăng ký
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String name,
                                  @RequestParam String username,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String phone,
                                  @RequestParam String houseNumber,
                                  @RequestParam String street,
                                  @RequestParam String ward,
                                  @RequestParam String district,
                                  @RequestParam String city,
                                  Model model) {

        if (userService.findByEmail(email) != null) {
            model.addAttribute("error", "Email đã tồn tại.");
            return "register";
        }

        // Ghép địa chỉ từ các phần
        String fullAddress = houseNumber + ", " + street + ", " + ward + ", " + district + ", " + city;

        // Tạo token xác nhận email
        String token = UUID.randomUUID().toString();

        // Tạo User mới (chưa kích hoạt)
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(fullAddress);
        user.setPasswordHash(password);
        user.setRole("user");
        user.setIsEmailVerified(false); // Chưa xác thực email
        user.setVerificationToken(token);

        // Lưu User tạm thời
        userService.registerUser(user);

        // Gửi email xác nhận
        String verificationLink = "http://localhost:8080/verify-email?token=" + token;
        String emailContent = "<p>Chào " + username + ",</p>"
                + "<p>Vui lòng nhấn vào liên kết dưới đây để xác nhận email và kích hoạt tài khoản:</p>"
                + "<a href='" + verificationLink + "'>Xác nhận Email</a>";
        emailService.sendEmail(email, "Xác nhận Email", emailContent);

        model.addAttribute("message", "Liên kết xác thực đã được gửi đến email của bạn. Vui lòng xác thực để hoàn tất đăng ký!");
        return "register";
    }


    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "redirect:/login";
        }

        // Đặt trạng thái xác thực email
        user.setIsEmailVerified(true);
        user.setVerificationToken(null);
        userService.updateUser(user);

        // Truyền thông báo thành công
        redirectAttributes.addFlashAttribute("success", "Xác thực tài khoản thành công!");

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);

        if (user == null) {
            model.addAttribute("error", "Không tìm thấy thông tin người dùng.");
            return "profile";
        }
        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
            return "error";
        }

        // Lấy tổng chi tiêu của người dùng
        double totalSpending = orderService.getTotalSpending(userId);

        // Xác định hạng thẻ thành viên
        String membershipTier;
        if (totalSpending < 5_000_000) {
            membershipTier = "Đồng";
        } else if (totalSpending < 10_000_000) {
            membershipTier = "Bạc";
        } else if (totalSpending < 20_000_000) {
            membershipTier = "Vàng";
        } else {
            membershipTier = "Bạch Kim";
        }

        // Định dạng tổng chi tiêu
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedTotalSpending = formatter.format(totalSpending);

        List<Map<String, Object>> top5MenuItems = orderService.getTop5MenuItemsByUserId(userId);
        model.addAttribute("top5MenuItems", top5MenuItems);

        List<Map<String, Object>> pointsHistory = orderService.getPointsHistoryByUserId(userId);
        model.addAttribute("pointsHistory", pointsHistory);

        // Gửi thông tin vào model
        model.addAttribute("user", user);
        model.addAttribute("totalSpending", formattedTotalSpending);
        model.addAttribute("membershipTier", membershipTier); // Gửi hạng thẻ thành viên
        model.addAttribute("page", "customer");

        return "profile";
    }
    @GetMapping("/profile-test")
    public String showProfilePage2(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);

        if (user == null) {
            model.addAttribute("error", "Không tìm thấy thông tin người dùng.");
            return "profile";
        }
        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
            return "error";
        }

        // Lấy tổng chi tiêu của người dùng
        double totalSpending = orderService.getTotalSpending(userId);

        // Xác định hạng thẻ thành viên
        String membershipTier;
        if (totalSpending < 5_000_000) {
            membershipTier = "Đồng";
        } else if (totalSpending < 10_000_000) {
            membershipTier = "Bạc";
        } else if (totalSpending < 20_000_000) {
            membershipTier = "Vàng";
        } else {
            membershipTier = "Bạch Kim";
        }

        // Định dạng tổng chi tiêu
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedTotalSpending = formatter.format(totalSpending);

        List<Map<String, Object>> top5MenuItems = orderService.getTop5MenuItemsByUserId(userId);
        model.addAttribute("top5MenuItems", top5MenuItems);

        List<Map<String, Object>> pointsHistory = orderService.getPointsHistoryByUserId(userId);
        model.addAttribute("pointsHistory", pointsHistory);

        // Gửi thông tin vào model
        model.addAttribute("user", user);
        model.addAttribute("totalSpending", formattedTotalSpending);
        model.addAttribute("membershipTier", membershipTier); // Gửi hạng thẻ thành viên
        model.addAttribute("page", "customer");

        return "profile-test";
    }

    

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name, 
                                @RequestParam String phone, 
                                @RequestParam String houseNumber,
                                @RequestParam String street,
                                @RequestParam String ward,
                                @RequestParam String district,
                                @RequestParam String city,
                                @RequestParam(required = false) String password,
                                HttpSession session, 
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/profile";
        }

        if (user.isForceRelogin()) {
            session.invalidate(); // Hủy session để buộc đăng nhập lại
            return "redirect:/login?expired=true";
        }

        try {
            boolean isUpdated = userService.updateUserProfile(userId, name , phone, houseNumber, street, ward, district, city, password);
            
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Cập nhật không thành công.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile"; 
    }

 // Bước 1: Nhập email hiện tại và nhận mã OTP
    @PostMapping("/profile/request-email-change")
    public String requestEmailChange(@RequestParam String currentEmail, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/profile";
        }

        // Kiểm tra nếu email hiện tại đúng
        if (!user.getEmail().equals(currentEmail)) {
            redirectAttributes.addFlashAttribute("error", "Email hiện tại không đúng.");
            return "redirect:/profile";
        }

        // Tạo mã OTP ngẫu nhiên gồm 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setEmailVerificationCode(otp);
        userService.updateUser(user);

        // Gửi mã OTP qua email hiện tại
        String emailContent = "Mã xác thực thay đổi email của bạn là: " + otp;
        emailService.sendEmail(currentEmail, "Xác Thực Email", emailContent);

        redirectAttributes.addFlashAttribute("message", "Mã xác thực đã được gửi đến email của bạn.");
        return "redirect:/profile";
    }

    // Bước 2: Nhập mã OTP xác nhận email hiện tại
    @PostMapping("/profile/verify-email-code")
    public String verifyEmailCode(@RequestParam String otp, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null || !otp.equals(user.getEmailVerificationCode())) {
            redirectAttributes.addFlashAttribute("error", "Mã xác thực không đúng.");
            return "redirect:/profile";
        }

        // Mã OTP hợp lệ, cho phép người dùng nhập email mới
        redirectAttributes.addFlashAttribute("message", "Mã xác thực thành công. Bạn có thể nhập email mới.");
        return "redirect:/profile";
    }

    // Bước 3: Nhập email mới và xác nhận email mới
    @PostMapping("/profile/confirm-new-email")
    public String confirmNewEmail(@RequestParam String newEmail, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/profile";
        }

        // Cập nhật email mới cho người dùng
        user.setEmail(newEmail);
        user.setEmailVerificationCode(null);  // Xóa mã OTP sau khi xác nhận
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("success", "Email của bạn đã được cập nhật thành công.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword, 
                                 @RequestParam String newPassword,
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user != null && userService.authenticate(user.getUsername(), currentPassword)) {
            userService.updatePassword(user.getUsername(), newPassword);
            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được thay đổi thành công.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác.");
        }
        return "redirect:/profile";
    }
	@GetMapping("/forgot-password")
	    public String showForgotPasswordPage() {
	        return "forgot-password";
	    }
	    public String generateResetPasswordEmailContent(String resetLink, String username) {
	        return "<div class=\"\"><div class=\"aHl\"></div><div id=\":n8\" tabindex=\"-1\"></div><div id=\":n1\" class=\"ii gt\" jslog=\"20277; u014N:xr6bB; 1:WyIjdGhyZWFkLWY6MTUzNzYzOTMzNTI2MDM0Nzk0NSJd; 4:WyIjbXNnLWY6MTUzNzYzOTMzNTI2MDM0Nzk0NSIsbnVsbCxudWxsLG51bGwsMSwwLFsxLDAsMF0sOTAsNTYwLG51bGwsbnVsbCxudWxsLG51bGwsbnVsbCwxLG51bGwsbnVsbCxbMF0sbnVsbCxudWxsLG51bGwsbnVsbCxudWxsLG51bGwsMF0.\"><div id=\":pt\" class=\"a3s aiL \"><u></u>\r\n"
	        		+ "\r\n"
	        		+ "\r\n"
	        		+ "    \r\n"
	        		+ "    \r\n"
	        		+ "    \r\n"
	        		+ "    \r\n"
	        		+ "    \r\n"
	        		+ "\r\n"
	        		+ "\r\n"
	        		+ "    <div>\r\n"
	        		+ "        <div id=\"m_-73110029099175430mailContentContainer\">\r\n"
	        		+ "            <div style=\"background:#efefef\">\r\n"
	        		+ "    \r\n"
	        		+ "                <table width=\"700\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-family:Microsoft Yahei\">\r\n"
	        		+ "                    <tbody>\r\n"
	        		+ "                        <tr>\r\n"
	        		+ "                            <td valign=\"bottom\"></td>\r\n"
	        		+ "                            <td height=\"20\" align=\"right\" valign=\"bottom\" style=\"font-size:12px\">\r\n"
	        		+ "                                &nbsp;\r\n"
	        		+ "                            </td>\r\n"
	        		+ "                        </tr>\r\n"
	        		+ "                        <tr>\r\n"
	        		+ "                            <td width=\"150\" valign=\"center\">\r\n"
	        		+ "                                <a href=\"http://www.net.cn/\" target=\"_blank\" data-saferedirecturl=\"https://www.google.com/url?q=http://www.net.cn/&amp;source=gmail&amp;ust=1738959143770000&amp;usg=AOvVaw3HFcrdmN0S9OYJeDKmQ67w\"><img src=\"https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa.jpg\" width=\"50px\" class=\"CToWUd\" data-bit=\"iit\"></a>\r\n"
	        		+ "                            </td>\r\n"
	        		+ "                            <td width=\"550\" valign=\"bottom\">\r\n"
	        		+ "                                <div style=\"padding:0 4px 5px 0;text-align:left;font-size:12px;color:#999999\">\r\n"
	        		+ "                                  This letter is automatically e-mail, please do not reply directly.\r\n"
	        		+ "                                </div>\r\n"
	        		+ "                            </td>\r\n"
	        		+ "                        </tr>\r\n"
	        		+ "                    </tbody>\r\n"
	        		+ "                </table>\r\n"
	        		+ "    \r\n"
	        		+ "                <table width=\"700\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\r\n"
	        		+ "                    <tbody>\r\n"
	        		+ "                        <tr>\r\n"
	        		+ "                            <td width=\"700\" height=\"22\" style=\"background:url(https://ci3.googleusercontent.com/meips/ADKq_Na91Cj1n3_1iOa3PhhcDWW_R4sr8jsJgG4zDr67gUYGn5mSpBWynyT10jkxJNF5WfB3eBBtuqs-RrU6RYJruRCEhr56RT20HV0eg3oy_Buu14I0Demsk-eF9_kxV6sH=s0-d-e1-ft#http://sdk.pocketgamesol.com:8091/resetPwd/assets/img/email_header_bg.png) right\"></td>\r\n"
	        		+ "                        </tr>\r\n"
	        		+ "                    </tbody>\r\n"
	        		+ "                </table>\r\n"
	        		+ "                <table width=\"700\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#ffffff;margin:0 auto;font-family:'microsoft yahei'\">\r\n"
	        		+ "                    <tbody>\r\n"
	        		+ "                        <tr>\r\n"
	        		+ "                            <td>\r\n"
	        		+ "                                <table width=\"700\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\r\n"
	        		+ "                                    <tbody>\r\n"
	        		+ "                                        <tr>\r\n"
	        		+ "                                            <td width=\"50%\">\r\n"
	        		+ "                                                <div style=\"padding:15px 0 0 25px;float:left;font-size:14px;font-family:Microsoft Yahei\">\r\n"
	                + "                                                   Chào bạn <span style=\"color:#ff6600\">" + username + "</span>\r\n"
	        		+ "                                                </div>\r\n"
	        		+ "                                            </td>\r\n"
	        		+ "                                            <td width=\"50%\"></td>\r\n"
	        		+ "                                        </tr>\r\n"
	        		+ "                                    </tbody>\r\n"
	        		+ "                                </table>\r\n"
	        		+ "    \r\n"
	        		+ "    \r\n"
	        		+ "                                <table width=\"700\" border=\"0\" cellspacing=\"8\" cellpadding=\"0\">\r\n"
	        		+ "                                    <tbody>\r\n"
	        		+ "                                        <tr>\r\n"
	        		+ "                                            <td>\r\n"
	        		+ "                                                <div style=\"margin:0 25px 0 25px;padding:10px 0 0 0;line-height:24px;font-size:14px;font-family:Microsoft Yahei\">\r\n"
	        		+ "                                                    <div style=\"color:#ff6600;width:650px;font-size:24px;text-align:center;margin:20px 0 25px\">Hướng dẫn cách reset mật khẩu</div>\r\n"
	        		+ "                                                    <p>Đây là email xác nhận yêu cầu đổi mật khẩu của tài khoản Sushi Wa. Click vào đây để reset mật khẩu. (Yêu cầu sẽ hết hạn sau 10 phút.) </p>\r\n"
	                + "                                                    <div style=\"width:100%;text-align:center;height:40px;padding-top:24px\">\r\n"
	        		+ "                                                        <a href=\"" + resetLink + "\" style=\"text-decoration:none;border:none;background-color:#fd9200;border-radius:3px;height:40px;font-size:18px;color:white;width:190px;margin:20px 0 30px;font-family:'Microsoft Yahei';padding:8px 21px;color:#ffffff\" target=\"_blank\">Click here to change your password</a>\r\n"
	        		+ "                                                    </div>\r\n"
	        		+ "                                                    <p style=\"padding:0 0 15px 0;line-height:25px\">Hoặc sao chép liên kết sau để mở trong trình duyệt.</p>\r\n"
	        		+ "                                                    <p style=\"padding:0 0 15px 0;line-height:25px\">\r\n"
	        		+ "                                                        <a style=\"color:#0088cc;text-decoration:underline;word-break:break-word;display:block;max-width:100%;overflow-wrap:anywhere;font-size:12px\" href=\"" + resetLink + "\" target=\"_blank\">\r\n"
	        		+ "                                                            " + resetLink + "\r\n"
	        		+ "                                                        </a>\r\n"
	        		+ "                                                    </p>\r\n"
	        		+ "                                                    <p>Nếu vẫn còn thắc mắc, xin vào trang chủ hoặc fanpage để liên hệ với CSKH. Trân trọng。</p>\r\n"
	        		+ "                                                </div>\r\n"
	        		+ "    \r\n"
	        		+ "                                            </td>\r\n"
	        		+ "                                        </tr>\r\n"
	        		+ "                                    </tbody>\r\n"
	        		+ "                                </table>\r\n"
	        		+ "    \r\n"
	        		+ "                            </td>\r\n"
	        		+ "                        </tr>\r\n"
	        		+ "                    </tbody>\r\n"
	        		+ "                </table>\r\n"
	        		+ "    \r\n"
	        		+ "                <table width=\"700\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\r\n"
	        		+ "                    <tbody>\r\n"
	        		+ "                        <tr>\r\n"
	        		+ "                            <td width=\"700\" height=\"22\" style=\"background:url(https://ci3.googleusercontent.com/meips/ADKq_Na3G80tMKVb2XADHVI3D2tSV6tNM5emlFCAQ45X31hzD2pZfCiVak_5NGrteZWxC5bCuyiK0n5cc1k82YKnPqC7T6r4-yB37GBVNAQ99KEnM3sunukB5lNzBDP2wqbv=s0-d-e1-ft#http://sdk.pocketgamesol.com:8091/resetPwd/assets/img/email_footer_bg.png) 0 0 no-repeat\"></td>\r\n"
	        		+ "                        </tr>\r\n"
	        		+ "                    </tbody>\r\n"
	        		+ "                </table><div class=\"yj6qo\"></div><div class=\"adL\">\r\n"
	        		+ "    \r\n"
	        		+ "            </div></div><div class=\"adL\">\r\n"
	        		+ "    \r\n"
	        		+ "        </div></div><div class=\"adL\">\r\n"
	        		+ "    </div></div><div class=\"adL\">\r\n"
	        		+ "    \r\n"
	        		+ "    </div></div></div><div class=\"WhmR8e\" data-hash=\"0\"></div></div>";
	    }

	    @PostMapping("/forgot-password")
	    @ResponseBody // Bắt buộc để trả về JSON hoặc String trực tiếp
	    public ResponseEntity<String> processForgotPassword(@RequestParam String email) {
	        User user = userService.findByEmail(email);
	        if (user == null) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email không tồn tại.");
	        }

	        String token = userService.createPasswordResetToken(user);
	        String resetLink = "http://localhost:8080/reset-password?token=" + token;

	        String emailContent = generateResetPasswordEmailContent(resetLink, user.getUsername());
	        emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", emailContent);

	        return ResponseEntity.ok("Liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
	    }


    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password"; // Trang nhập mật khẩu mới
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token, 
                                       @RequestParam String newPassword, Model model) {
        if (userService.resetPassword(token, newPassword)) {
            model.addAttribute("message", "Mật khẩu đã được cập nhật thành công.");
            return "login"; // Chuyển về trang đăng nhập sau khi đổi mật khẩu thành công
        } else {
            model.addAttribute("error", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "reset-password";
        }
    }
    @GetMapping("/contact")
    public String showContactPage( Model model, HttpSession session) {
        model.addAttribute("page", "customer");

        String username = (String) session.getAttribute("loggedInUser");
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) { // Kiểm tra xem người dùng đã đăng nhập hay chưa
            User user = userService.getUserById(userId);

            if (user.isForceRelogin()) { // Kiểm tra trạng thái buộc đăng nhập lại
                model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
                return "error"; // Chuyển hướng đến trang lỗi
            }

            // Lấy thông báo cho người dùng đã đăng nhập
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();

            // Sắp xếp thông báo theo thời gian (mới nhất lên đầu)
            notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

            // Thêm thông báo và số lượng chưa đọc vào model
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadNotificationsCount", unreadCount);
        }
        return "contact"; // Tên của template cho trang đăng nhập
    }

    @GetMapping("/customer")
    public String showCustomerPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để tiếp tục.");
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);

        if (user == null) {
            model.addAttribute("error", "Không tìm thấy thông tin người dùng.");
            return "profile";
        }
        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        // Lấy tổng chi tiêu của người dùng
        double totalSpending = orderService.getTotalSpending(userId);

        // Định dạng tổng chi tiêu và điểm khả dụng
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedTotalSpending = formatter.format(totalSpending);
        String formattedPoints = formatter.format(user.getPoints());

        // Lấy các thông tin bổ sung
        List<Map<String, Object>> top5MenuItems = orderService.getTop5MenuItemsByUserId(userId);
        List<Map<String, Object>> pointsHistory = orderService.getPointsHistoryByUserId(userId);

        // Gửi thông tin vào model
        model.addAttribute("user", user);
        model.addAttribute("totalSpending", formattedTotalSpending);
        model.addAttribute("formattedPoints", formattedPoints); // Gửi điểm đã định dạng
        model.addAttribute("top5MenuItems", top5MenuItems);
        model.addAttribute("pointsHistory", pointsHistory);
        // Xác định trang hiện tại
        model.addAttribute("page", "customer");
        return "customer"; // trả về tên của trang Thymeleaf
    }

}