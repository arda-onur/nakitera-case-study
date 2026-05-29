package com.ardao.nakitera_case_study.response;

import com.ardao.nakitera_case_study.enums.Side;
import com.ardao.nakitera_case_study.enums.Status;

import java.time.Instant;

public record OrderListResponse(long id,
                                long customerId,
                                String assetName,
                                int size,
                                int price,
                                Side orderSide,
                                Status orderStatus,
                                Instant createDate) {
}
