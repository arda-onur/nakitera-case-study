package com.ardao.nakitera_case_study.repository.box;

import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutbox,Long> {

    List<OrderOutbox> findByPublishedAndEventTypeInOrderByIdAsc(boolean published, Collection<String> eventTypes);
}
