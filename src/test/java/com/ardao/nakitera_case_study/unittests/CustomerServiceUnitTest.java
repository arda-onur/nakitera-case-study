package com.ardao.nakitera_case_study.unittests;

import com.ardao.nakitera_case_study.entity.Asset;
import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.service.AssetService;
import com.ardao.nakitera_case_study.service.CustomerService;
import com.ardao.nakitera_case_study.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceUnitTest {
    @Mock
    private AssetService assetService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createCustomer_shouldCreateCustomerWithTryAssetAndSaveIt() {
        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(5000);
        tryAsset.setUsableSize(5000);

        when(assetService.createTRYAsset(any(Customer.class))).thenReturn(tryAsset);

        Customer createdCustomer = customerService.createCustomer();

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();

        assertSame(savedCustomer, createdCustomer);

    }
}
