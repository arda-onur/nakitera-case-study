package com.ardao.nakitera_case_study.service;

import com.ardao.nakitera_case_study.entity.Asset;
import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.Outbox;

public interface AssetService {
    Asset createTRYAsset(Customer customer);
    void handleOrderCreatedBuyEvent(Outbox outboxEvent);
    void handleOrderCreatedSellEvent(Outbox outboxEvent);
}
