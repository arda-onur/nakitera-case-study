package com.ardao.nakitera_case_study.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Locale;

public record OrderRequest(

        Long customerId,

        @NotBlank(message = "{order.request.validation.asset.name.cannot.be.blank}")
        String assetName,

        @NotNull(message = "{order.request.validation.order.side.cannot.be.null}")
        String orderSide,

        @Positive(message = "{order.request.validation.size.positive}")
        int size,

        @Positive(message = "{order.request.validation.price.positive}")
        int price
) {
    public OrderRequest{
        assetName = assetName.toUpperCase(Locale.ROOT).trim();
        orderSide = orderSide.toUpperCase(Locale.ROOT).trim();
    }

}
