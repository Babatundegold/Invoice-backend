package com.invoicesaas.repository;

import com.invoicesaas.entity.Invoice;
import com.invoicesaas.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCompanyId(Long companyId);
    List<Invoice> findByCompanyIdAndStatus(Long companyId, InvoiceStatus status);
    List<Invoice> findByClientId(Long clientId);
    Optional<Invoice> findByPublicToken(String publicToken);
    long countByCompanyId(Long companyId);

    // for free-tier monthly invoice limit check
    long countByCompanyIdAndCreatedAtBetween(Long companyId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
