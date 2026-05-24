package com.ardao.nakitera_case_study.entity;

import com.ardao.nakitera_case_study.enums.Side;
import com.ardao.nakitera_case_study.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(nullable = false, updatable = false)
    private String assetName;
    private int size;
    private int price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side orderSide;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status orderStatus = Status.PENDING;
    @Column(nullable = false, updatable = false)
    private Instant createDate;

    @PrePersist
    void prePersist() {
        this.createDate = Instant.now();
    }
}
