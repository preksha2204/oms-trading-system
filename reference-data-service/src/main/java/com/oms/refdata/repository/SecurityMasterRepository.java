package com.oms.refdata.repository;

import com.oms.refdata.entity.SecurityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityMasterRepository extends JpaRepository<SecurityMaster, Long> {
    Optional<SecurityMaster> findBySymbol(String symbol);
    List<SecurityMaster> findByUnderlyingSymbol(String underlyingSymbol);
    List<SecurityMaster> findByIsActiveTrue();
    boolean existsBySymbolAndIsActiveTrue(String symbol);
}
