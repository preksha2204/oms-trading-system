package com.oms.refdata.service;

import com.oms.refdata.cache.SecurityMasterCache;
import com.oms.refdata.entity.CustomerMaster;
import com.oms.refdata.entity.SecurityMaster;
import com.oms.refdata.repository.CustomerMasterRepository;
import com.oms.refdata.repository.SecurityMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferenceDataService {

    private final SecurityMasterCache      securityCache;
    private final SecurityMasterRepository securityRepo;
    private final CustomerMasterRepository customerRepo;

    public boolean isValidSymbol(String symbol) {
        return securityCache.isValidSymbol(symbol);
    }

    public Optional<SecurityMaster> getSecurity(String symbol) {
        return Optional.ofNullable(securityCache.get(symbol));
    }

    public Optional<CustomerMaster> getCustomer(String customerId) {
        return customerRepo.findByCustomerIdAndIsActiveTrue(customerId);
    }

    public List<SecurityMaster> getOptionsByUnderlying(String underlying) {
        return securityCache.getOptionsByUnderlying(underlying);
    }

    public List<SecurityMaster> getAllActiveSecurities() {
        return securityRepo.findByIsActiveTrue();
    }

    public void reloadCache() {
        securityCache.reload();
    }
}
