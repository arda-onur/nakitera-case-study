package com.ardao.nakitera_case_study.service.impl;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.enums.Role;
import com.ardao.nakitera_case_study.exception.custom.UserAlreadyExistException;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.UserRepository;
import com.ardao.nakitera_case_study.service.AuthService;
import com.ardao.nakitera_case_study.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, CustomerService customerService, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    @Override
    public void createUser(User newUser) {
        log.info("Creating user. username={}", newUser.getUsername());
        Optional<User> isUserInDB = this.userRepository.findUserByUsername(newUser.getUsername());
        if (isUserInDB.isPresent()) {
            log.warn("User creation rejected because username already exists. username={}", newUser.getUsername());
            throw new UserAlreadyExistException("user.already.exists.exception", newUser.getUsername());
        }
        Customer newCustomer = this.customerService.createCustomer();
                 newCustomer.setUser(newUser);
                 newCustomer.getUser().setPassword(this.passwordEncoder.encode(newUser.getPassword()));
                 newCustomer.getUser().setRole(Role.ROLE_USER);
                 newCustomer.getUser().setCustomer(newCustomer);

        this.customerRepository.save(newCustomer);
        log.info("User created successfully. username={}, customerId={}",
                newUser.getUsername(), newCustomer.getId());
    }
}
