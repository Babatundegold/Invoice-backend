package com.invoicesaas.service;

import com.invoicesaas.dto.ClientDto;
import com.invoicesaas.entity.Client;
import com.invoicesaas.entity.Company;
import com.invoicesaas.exception.ResourceNotFoundException;
import com.invoicesaas.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final CompanyService companyService;

    public Client create(Long ownerId, ClientDto dto) {
        Company company = companyService.getByOwnerId(ownerId);
        Client client = Client.builder()
                .company(company)
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();
        return clientRepository.save(client);
    }

    public List<Client> list(Long ownerId, String search) {
        Company company = companyService.getByOwnerId(ownerId);
        if (search != null && !search.isBlank()) {
            return clientRepository.findByCompanyIdAndNameContainingIgnoreCase(company.getId(), search);
        }
        return clientRepository.findByCompanyId(company.getId());
    }

    public Client get(Long ownerId, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        validateOwnership(ownerId, client);
        return client;
    }

    public Client update(Long ownerId, Long clientId, ClientDto dto) {
        Client client = get(ownerId, clientId);
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        return clientRepository.save(client);
    }

    public void delete(Long ownerId, Long clientId) {
        Client client = get(ownerId, clientId);
        clientRepository.delete(client);
    }

    private void validateOwnership(Long ownerId, Client client) {
        Company company = companyService.getByOwnerId(ownerId);
        if (!client.getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Client not found");
        }
    }
}
