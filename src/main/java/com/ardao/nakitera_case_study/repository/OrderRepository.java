package com.ardao.nakitera_case_study.repository;

import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.enums.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Order> findAllByOrderStatus(Status orderStatus);
    Page<Order> getOrdersByCustomer_IdAndCreateDateBetween(long customerId,
                                                           Instant createDateAfter,
                                                           Instant createDateBefore,
                                                           Pageable pageable);

}
