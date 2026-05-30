package com.ardao.nakitera_case_study.unittests;


import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.enums.Role;
import com.ardao.nakitera_case_study.exception.custom.UserAlreadyExistException;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.UserRepository;
import com.ardao.nakitera_case_study.service.CustomerService;
import com.ardao.nakitera_case_study.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void createUser_shouldCreateUserAndSaveCustomer() {
        User newUser = new User();
        newUser.setUsername("arda");
        newUser.setPassword("12345");

        Customer newCustomer = new Customer();

        when(userRepository.findUserByUsername("arda")).thenReturn(Optional.empty());
        when(customerService.createCustomer()).thenReturn(newCustomer);
        when(passwordEncoder.encode("12345")).thenReturn("encoded-password");

        authService.createUser(newUser);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();

        assertSame(newCustomer, savedCustomer);
        assertSame(newUser, savedCustomer.getUser());
        assertSame(savedCustomer, newUser.getCustomer());
        assertEquals(Role.ROLE_USER, newUser.getRole());
        assertEquals("encoded-password", newUser.getPassword());

        verify(userRepository).findUserByUsername("arda");
        verify(customerService).createCustomer();
        verify(passwordEncoder).encode("12345");
    }

    @Test
    void createUser_shouldThrowWhenUsernameAlreadyExists() {
        User existingUser = new User();
        existingUser.setUsername("arda");

        User newUser = new User();
        newUser.setUsername("arda");
        newUser.setPassword("12345");

        when(userRepository.findUserByUsername("arda")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistException.class, () -> authService.createUser(newUser));

        verify(customerService, never()).createCustomer();
        verify(customerRepository, never()).save(any(Customer.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}