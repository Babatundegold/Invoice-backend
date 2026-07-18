package com.invoicesaas.service;

import com.invoicesaas.dto.CompanyDto;
import com.invoicesaas.entity.Company;
import com.invoicesaas.exception.ResourceNotFoundException;
import com.invoicesaas.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final FileStorageService fileStorageService;

    public Company getByOwnerId(Long ownerId) {
        return companyRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
    }

    public Company updateProfile(Long ownerId, CompanyDto dto) {
        Company company = getByOwnerId(ownerId);
        company.setBusinessName(dto.getBusinessName());
        company.setAddress(dto.getAddress());
        company.setPhone(dto.getPhone());
        company.setTaxId(dto.getTaxId());
        company.setBankName(dto.getBankName());
        company.setBankAccountNumber(dto.getBankAccountNumber());
        company.setBankAccountName(dto.getBankAccountName());
        if (dto.getCurrency() != null) company.setCurrency(dto.getCurrency());
        if (dto.getInvoicePrefix() != null) company.setInvoicePrefix(dto.getInvoicePrefix());
        // logoUrl is intentionally NOT set here - logo is optional and managed
        // exclusively via uploadLogo()/removeLogo() below.
        return companyRepository.save(company);
    }

    /**
     * Logo is OPTIONAL. If the user never calls this, company.logoUrl stays null
     * and invoice PDFs simply render the business name as text instead of an image.
     */
    public Company uploadLogo(Long ownerId, MultipartFile file) {
        Company company = getByOwnerId(ownerId);

        // replace old logo if one exists
        if (company.getLogoUrl() != null) {
            fileStorageService.deleteLogo(company.getLogoUrl());
        }

        String logoUrl = fileStorageService.storeLogo(file);
        company.setLogoUrl(logoUrl);
        return companyRepository.save(company);
    }

    public Company removeLogo(Long ownerId) {
        Company company = getByOwnerId(ownerId);
        if (company.getLogoUrl() != null) {
            fileStorageService.deleteLogo(company.getLogoUrl());
            company.setLogoUrl(null);
        }
        return companyRepository.save(company);
    }
}
