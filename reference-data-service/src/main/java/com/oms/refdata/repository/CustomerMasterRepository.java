package com.oms.refdata.repository;

import com.oms.refdata.entity.CustomerMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerMasterRepository extends JpaRepository<CustomerMaster, String> {
    Optional<CustomerMaster> findByCustomerIdAndIsActiveTrue(String customerId);
}
