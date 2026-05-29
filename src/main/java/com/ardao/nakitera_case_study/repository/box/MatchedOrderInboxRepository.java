package com.ardao.nakitera_case_study.repository.box;

import com.ardao.nakitera_case_study.entity.box.MatchedOrderInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MatchedOrderInboxRepository extends JpaRepository<MatchedOrderInbox,Long> {
    boolean existsByMatchedOutboxId(Long matchedOutboxId);
}
