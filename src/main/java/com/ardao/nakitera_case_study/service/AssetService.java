package com.ardao.nakitera_case_study.service;

import com.ardao.nakitera_case_study.entity.Asset;
import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.event.dto.MatchedOrderEvent;
import com.ardao.nakitera_case_study.event.dto.OrderEvent;
import com.ardao.nakitera_case_study.response.AssetListResponse;
import org.springframework.data.domain.Page;

public interface AssetService {
    Asset createTRYAsset(Customer customer);
    void handleOrderCreatedBuyEvent(OrderEvent orderEvent);
    void handleOrderCreatedSellEvent(OrderEvent orderEvent);
    void releaseReservedBuyOrder(OrderEvent orderEvent);
    void releaseReservedSellOrder(OrderEvent orderEvent);
    void handleMatchedOrderEvent(MatchedOrderEvent matchedOrderEvent);
    Page<AssetListResponse> getCustomerAssetList(int page, int size, Long customerId);
}
