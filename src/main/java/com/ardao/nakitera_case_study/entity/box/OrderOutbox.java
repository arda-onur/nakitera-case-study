package com.ardao.nakitera_case_study.entity.box;

import com.ardao.nakitera_case_study.enums.Side;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side orderSide;

    @Column(nullable = false)
    private int size;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private boolean published = false;
}
