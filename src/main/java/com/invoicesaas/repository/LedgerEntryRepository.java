package com.invoicesaas.repository;

import com.invoicesaas.entity.LedgerEntry;
import com.invoicesaas.entity.LedgerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByCompanyIdOrderByDateDescCreatedAtDesc(Long companyId);
    List<LedgerEntry> findByCompanyIdAndTypeOrderByDateDesc(Long companyId, LedgerType type);
    List<LedgerEntry> findByCompanyIdAndDateBetween(Long companyId, LocalDate start, LocalDate end);
}
