package com.invoicesaas.controller;

import com.invoicesaas.dto.LedgerDto;
import com.invoicesaas.entity.LedgerEntry;
import com.invoicesaas.security.CurrentUser;
import com.invoicesaas.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * The "book" endpoints - lets the user log and browse sales, debts (owed to
 * them and owed by them), and expenses, similar to a physical record book.
 */
@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<LedgerEntry> create(@Valid @RequestBody LedgerDto dto) {
        return ResponseEntity.ok(ledgerService.create(CurrentUser.id(), dto));
    }

    @GetMapping
    public ResponseEntity<List<LedgerEntry>> list(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(ledgerService.list(CurrentUser.id(), type));
    }

    @GetMapping("/range")
    public ResponseEntity<List<LedgerEntry>> listByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ledgerService.listByDateRange(CurrentUser.id(), start, end));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LedgerEntry> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ledgerService.updateStatus(CurrentUser.id(), id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ledgerService.delete(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }
}
