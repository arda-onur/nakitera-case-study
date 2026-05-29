package com.ardao.nakitera_case_study.util.resolver;

import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.exception.custom.CustomerIdRequiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public final class CustomerIdResolver{
    public long resolveCustomerId(Long customerId){
        Authentication authentication = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication());
        log.info("Resolving customer id.");

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if(isAdmin){
            if (customerId == null)
                throw new CustomerIdRequiredException("customer.id.required.exception");
            return customerId;
        }
        User currentUser = (User)authentication.getPrincipal();

        return currentUser.getCustomer().getId();
    }
}
