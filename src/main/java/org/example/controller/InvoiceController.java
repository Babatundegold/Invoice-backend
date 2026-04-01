package org.example.controller;

import org.example.dto.request.InvoiceRequest;
import org.example.dto.response.InvoiceResponse;
import org.example.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin("*")
public class InvoiceController {

    @Autowired
    private InvoiceService service;

    // CREATE
    @PostMapping
    public InvoiceResponse create(@RequestBody InvoiceRequest request) {
        return service.createInvoice(request);
    }

    // GET ALL
    @GetMapping
    public List<InvoiceResponse> getAll() {
        return service.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public InvoiceResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable String id) {
        service.delete(id);
        return "Invoice deleted successfully";
    }
}