package com.ardao.nakitera_case_study.service.impl;

import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.service.AssetService;
import com.ardao.nakitera_case_study.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final AssetService assetService;
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(AssetService assetService, CustomerRepository customerRepository) {
        this.assetService = assetService;
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer createCustomer() {
        Customer newCustomer = new Customer();
        newCustomer.getAssetList().add(this.assetService.createTRYAsset(newCustomer));
        this.customerRepository.save(newCustomer);
        return newCustomer;
    }
}
