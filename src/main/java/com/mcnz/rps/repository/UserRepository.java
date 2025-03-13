package com.mcnz.rps.repository;

import com.mcnz.rps.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, Long> {
    // Define method to find user by username
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); 
    Optional<User> findByResetToken(String resetToken); // Thêm phương thức này
    Optional<User> findById(Long id);
    @Query("SELECT u FROM User u WHERE u.verificationToken = :token")
    User findByVerificationToken(@Param("token") String token);

    User findBySecureToken(String secureToken);
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

}
