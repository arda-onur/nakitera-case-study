package com.ardao.nakitera_case_study.repository;

import com.ardao.nakitera_case_study.entity.Asset;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Asset> getAssetByAssetNameAndCustomer_Id(String assetName, long customerId);

    Page<Asset> getAssetByCustomer_Id(long customerId, Pageable pageable);
}
