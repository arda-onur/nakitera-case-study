package com.ardao.nakitera_case_study.event.dto;

import com.ardao.nakitera_case_study.enums.Side;

public record AssetEvent(
                         Long outboxId,
                         Long orderId,
                         Long customerId,
                         String assetName,
                         String eventType,
                         Side orderSide,
                         int size,
                         int price) {
}

