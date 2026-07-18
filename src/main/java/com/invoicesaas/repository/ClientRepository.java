package com.invoicesaas.repository;

import com.invoicesaas.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByCompanyId(Long companyId);
    List<Client> findByCompanyIdAndNameContainingIgnoreCase(Long companyId, String name);
}
