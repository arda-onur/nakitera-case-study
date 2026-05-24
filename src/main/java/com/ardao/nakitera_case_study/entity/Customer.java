package com.ardao.nakitera_case_study.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.catalina.LifecycleState;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true,nullable = false)
    private long id;

    @OneToOne(mappedBy = "customer", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private User user;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<Asset> assetList = new ArrayList<>();

    @OneToMany(mappedBy = "customer")
    private List<Order> orderList = new ArrayList<>();
}
