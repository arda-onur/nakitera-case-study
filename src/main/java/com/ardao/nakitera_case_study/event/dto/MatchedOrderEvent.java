package com.ardao.nakitera_case_study.event.dto;

public record MatchedOrderEvent(
        Long outboxId,
        Long buyOrderId,
        Long sellOrderId,
        Long buyCustomerId,
        Long sellCustomerId,
        String assetName,
        int matchedSize,
        int buyPrice,
        int sellPrice
) {
}