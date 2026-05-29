package com.ardao.nakitera_case_study.service.userdetails;

import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.exception.custom.UserNotFoundException;
import com.ardao.nakitera_case_study.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Login attempt for username = {}", username);
        User user = this.userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("user.not.found.exception", username));
        log.info("User loaded successfully for authentication. username={}, role={}",
                user.getUsername(), user.getRole());
        return user;
    }
}
