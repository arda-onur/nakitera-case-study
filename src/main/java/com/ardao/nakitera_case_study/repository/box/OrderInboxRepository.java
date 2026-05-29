package com.ardao.nakitera_case_study.repository.box;

import com.ardao.nakitera_case_study.entity.box.OrderInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInboxRepository extends JpaRepository<OrderInbox,Long> {

    boolean existsByOutboxId(Long outboxId);
}
