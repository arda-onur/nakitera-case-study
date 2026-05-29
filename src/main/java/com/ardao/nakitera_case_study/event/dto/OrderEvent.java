package com.ardao.nakitera_case_study.event.dto;

import com.ardao.nakitera_case_study.enums.Side;

public record OrderEvent(
        Long outboxId,
        Long orderId,
        Long customerId,
        String assetName,
        Side orderSide,
        int size,
        int price,
        String eventType) {
}
