package com.mcnz.rps.service;

import com.mcnz.rps.model.Customer;
import com.mcnz.rps.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;

@Service
public class CustomerService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return User.withUsername(customer.getUsername())
                   .password(customer.getPasswordHash())
                   .roles(customer.getRole()) // Đảm bảo vai trò được lấy từ cơ sở dữ liệu
                   .build();
    }
}
