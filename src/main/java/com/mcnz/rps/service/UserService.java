package com.mcnz.rps.service;
import java.util.concurrent.ConcurrentHashMap;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserSession;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.repository.UserRepository;
import com.mcnz.rps.repository.UserSessionRepository;
import com.mcnz.rps.repository.VoucherRepository;
import com.mcnz.rps.repository.OrderRepository;
import com.mcnz.rps.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;



@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository OrderRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private OrderDetailRepository OrderDetailRepository;
   
    @Autowired
    private VoucherRepository voucherRepository;
    private Map<String, User> passwordResetTokens = new HashMap<>();

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra nếu người dùng đã đăng nhập
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName(); // Lấy username của người dùng hiện tại
            return findByUsername(username); // Tìm người dùng theo username
        }
        
        // Trả về null hoặc có thể ném exception tùy theo yêu cầu ứng dụng
        return null;
    }

    public boolean registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false; // Username already exists
        }
        user.setPasswordHash(encodePassword(user.getPasswordHash()));
        userRepository.save(user);
        return true;
    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getPasswordHash());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    public User findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier).orElse(null);
    }

    public boolean updatePassword(String username, String newPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            user.get().setPasswordHash(encodePassword(newPassword));
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUserProfile(Long userId, String name, String phone,
                                     String houseNumber, String street, String ward, 
                                     String district, String city, String password) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Cập nhật thông tin
            user.setName(name);
            user.setPhone(phone);
            user.setAddress(houseNumber + ", " + street + ", " + ward + ", " + district + ", " + city);

            // Cập nhật mật khẩu nếu có nhập mới
            if (password != null && !password.isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(password));
            }


            // Thêm log để kiểm tra thông tin người dùng trước khi lưu
            System.out.println("Cập nhật thông tin người dùng: " + user);

            // Lưu thông tin cập nhật
            userRepository.save(user);
            return true;
        }
        
        return false;
    }


    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean updateUser(User user) {
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean processCheckout(User user, int pointsToUse, String voucherCode) {
        int pointsValue = pointsToUse * 500; // 1 point = 500 VND
        if (user.getPoints() >= pointsToUse) {
            user.setPoints(user.getPoints() - pointsToUse);
            userRepository.save(user);
        }

        if (voucherCode != null && !voucherCode.isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(voucherCode).orElse(null);
            if (voucher != null && voucher.canUse()) {
                voucher.incrementUsage();
                voucherRepository.save(voucher);
            } else {
                throw new IllegalArgumentException("Voucher không hợp lệ hoặc đã hết lượt sử dụng");
            }
        }
        return true;
    }
    

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

 

    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    // Giới hạn số lần gửi email quên mật khẩu trong ngày
    private final int MAX_EMAILS_PER_DAY = 10;
    private Map<String, Integer> emailRequests = new HashMap<>();

    // Tạo token đặt lại mật khẩu
    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10)); // Token hết hạn sau 30 phút
        userRepository.save(user);
        return token;
    }

    // Xác nhận token và cập nhật mật khẩu
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(token);
        
        if (userOptional.isEmpty()) {
            return false; // Token không hợp lệ
        }
        
        User user = userOptional.get();

        // Kiểm tra hạn của token
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false; // Token đã hết hạn
        }

        // Cập nhật mật khẩu mới và xóa token
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return true;
    }

    public User validatePasswordResetToken(String token) {
        User user = userRepository.findByResetToken(token).orElse(null);
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return null; // Token không hợp lệ hoặc đã hết hạn
        }
        // Xóa token sau khi sử dụng
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return user;
    }


    // Reset email count at midnight
    public void resetEmailCount() {
        emailRequests.clear();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    public boolean addNewUser(User newUser) {
        // Kiểm tra username và email có tồn tại chưa
        Optional<User> existingUser = userRepository.findByUsername(newUser.getUsername());
        Optional<User> existingEmail = userRepository.findByEmail(newUser.getEmail());

        if (existingUser.isPresent() || existingEmail.isPresent()) {
            return false; // Username hoặc email đã tồn tại
        }

        // Mặc định điểm ban đầu nếu chưa có
        if (newUser.getPoints() == 0) {
            newUser.setPoints(0);
        }

        // Mã hóa mật khẩu nếu cần thiết (tùy thuộc vào yêu cầu bảo mật)
        newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));

        // Lưu khách hàng mới vào cơ sở dữ liệu
        userRepository.save(newUser);
        return true;
    }

    /**
     * Cập nhật thông tin người dùng bằng username.
     */
    public boolean updateUserByUsername(String username, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findByUsername(username);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setName(updatedUser.getName());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setPoints(updatedUser.getPoints());
            existingUser.setAccountLocked(updatedUser.isAccountLocked()); // Thêm trạng thái khóa
         // Cập nhật trạng thái xác thực email
            existingUser.setEmailVerified(updatedUser.isEmailVerified());
            // Cập nhật trạng thái buộc đăng nhập lại
            existingUser.setForceRelogin(updatedUser.isForceRelogin());
            userRepository.save(existingUser);
            return true;
        }
        return false;
    }


     
    @Transactional
    public String deleteUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Lấy danh sách các đơn hàng của người dùng
            List<Order> orders = OrderRepository.findByUser(user);

            // Xóa các chi tiết đơn hàng trước
            for (Order order : orders) {
                OrderDetailRepository.deleteByOrder(order);
            }

            // Xóa các đơn hàng
            OrderRepository.deleteAll(orders);

            // Xóa người dùng
            userRepository.delete(user);

            return user.getName();
        }
        return null;
    }
    private final ConcurrentHashMap<String, Boolean> userStatusMap = new ConcurrentHashMap<>();


    public void updateUserStatus(String username, boolean isOnline) {
        userStatusMap.put(username, isOnline); // Cập nhật trạng thái trong bộ nhớ
    }

    public ConcurrentHashMap<String, Boolean> getAllUserStatuses() {
        return userStatusMap;
    }
    public User findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }
    public User findByEmailVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }


    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
    public User findBySecureToken(String secureToken) {
        return userRepository.findBySecureToken(secureToken);
    }

    public boolean isEmailExist(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }

  
    public void loginUser(String username, HttpSession session) {
        User user = findByUsernameOrEmail(username);
        if (user == null) return;

        // Tạo và lưu thông tin phiên người dùng vào cơ sở dữ liệu
        String sessionId = session.getId();
        LocalDateTime loginTime = LocalDateTime.now();

        UserSession userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUser(user);
        userSession.setLoginTime(loginTime);

        userSessionRepository.save(userSession); // Lưu vào database
    }

    public void logoutUser(User user, HttpSession session) {
        // Xóa thông tin phiên đăng nhập của người dùng khỏi cơ sở dữ liệu
        Optional<UserSession> userSessionOpt = userSessionRepository.findBySessionId(session.getId());
        userSessionOpt.ifPresent(userSession -> userSessionRepository.delete(userSession));

        // Hủy session
        session.invalidate();
    }
}
