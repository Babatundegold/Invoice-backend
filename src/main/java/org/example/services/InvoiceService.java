package org.example.services;

import org.example.dto.request.InvoiceRequest;
import org.example.dto.response.InvoiceResponse;
import org.example.model.Invoice;
import org.example.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository repository;

    // CREATE INVOICE
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        double total = request.getItems()
                .stream()
                .mapToDouble(i -> i.getQuantity() * i.getPrice())
                .sum();

        Invoice invoice = new Invoice();
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setItems(request.getItems());
        invoice.setTotal(total);
        invoice.setCreatedAt(LocalDate.now());

        Invoice saved = repository.save(invoice);

        return mapToResponse(saved);
    }

    // GET ALL INVOICES
    public List<InvoiceResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public InvoiceResponse getById(String id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return mapToResponse(invoice);
    }

    // DELETE
    public void delete(String id) {
        repository.deleteById(id);
    }

    // MAPPER FUNCTION
    private InvoiceResponse mapToResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCustomerName(),
                invoice.getCustomerEmail(),
                invoice.getItems(),
                invoice.getTotal(),
                invoice.getCreatedAt()
        );
    }
}