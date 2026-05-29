package com.ardao.nakitera_case_study.entity.box;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MatchedOrderOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyOrderId;

    @Column(nullable = false)
    private Long sellOrderId;

    @Column(nullable = false)
    private Long buyCustomerId;

    @Column(nullable = false)
    private Long sellCustomerId;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false)
    private int matchedSize;

    @Column(nullable = false)
    private int buyPrice;

    @Column(nullable = false)
    private int sellPrice;

    @Column(nullable = false)
    private boolean published = false;
}
