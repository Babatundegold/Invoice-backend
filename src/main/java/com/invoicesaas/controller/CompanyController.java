package com.invoicesaas.controller;

import com.invoicesaas.dto.CompanyDto;
import com.invoicesaas.entity.Company;
import com.invoicesaas.security.CurrentUser;
import com.invoicesaas.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<Company> getProfile() {
        return ResponseEntity.ok(companyService.getByOwnerId(CurrentUser.id()));
    }

    @PutMapping
    public ResponseEntity<Company> updateProfile(@RequestBody CompanyDto dto) {
        return ResponseEntity.ok(companyService.updateProfile(CurrentUser.id(), dto));
    }

    // Logo upload is a SEPARATE, OPTIONAL endpoint - the profile works fine without ever calling this
    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public ResponseEntity<Company> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(companyService.uploadLogo(CurrentUser.id(), file));
    }

    @DeleteMapping("/logo")
    public ResponseEntity<Company> removeLogo() {
        return ResponseEntity.ok(companyService.removeLogo(CurrentUser.id()));
    }
}
