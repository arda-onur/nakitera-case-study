package com.ardao.nakitera_case_study.repository.box;

import com.ardao.nakitera_case_study.entity.box.MatchedOrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchedOrderOutboxRepository extends JpaRepository<MatchedOrderOutbox,Long> {
    List<MatchedOrderOutbox> findByPublished(boolean published);
}
