package com.invoicesaas.repository;

import com.invoicesaas.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceId(Long invoiceId);
    Optional<Payment> findByPaystackReference(String reference);
}
