package com.mcnz.rps.repository;

import com.mcnz.rps.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Tìm tất cả khách hàng
    List<Customer> findAll();

    // Tìm khách hàng theo id
    Customer findByCustomerId(Long customerId);
 // Find customer by username
    Customer findByUsername(String username);
    
}
