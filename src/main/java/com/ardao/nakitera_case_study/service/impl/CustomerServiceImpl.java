package com.ardao.nakitera_case_study.service.impl;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.service.AssetService;
import com.ardao.nakitera_case_study.service.CustomerService;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final AssetService assetService;
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(AssetService assetService, CustomerRepository customerRepository) {
        this.assetService = assetService;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer() {
        log.info("Creating customer.");
        Customer newCustomer = new Customer();
        newCustomer.getAssetList().add(this.assetService.createTRYAsset(newCustomer));
        this.customerRepository.save(newCustomer);
        log.info("Customer created successfully. customerId={}", newCustomer.getId());
        return newCustomer;
    }
}
