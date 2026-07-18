package com.invoicesaas.controller;

import com.invoicesaas.dto.ClientDto;
import com.invoicesaas.entity.Client;
import com.invoicesaas.security.CurrentUser;
import com.invoicesaas.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<Client> create(@Valid @RequestBody ClientDto dto) {
        return ResponseEntity.ok(clientService.create(CurrentUser.id(), dto));
    }

    @GetMapping
    public ResponseEntity<List<Client>> list(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(clientService.list(CurrentUser.id(), search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> get(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.get(CurrentUser.id(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable Long id, @Valid @RequestBody ClientDto dto) {
        return ResponseEntity.ok(clientService.update(CurrentUser.id(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }
}
