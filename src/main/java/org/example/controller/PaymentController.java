package org.example.controller;

import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentResponse;
import org.example.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initialize")
    public PaymentResponse initialize(@RequestBody PaymentRequest request) {

        String url = paymentService.initializePayment(
                request.getEmail(),
                request.getAmount()
        );

        return new PaymentResponse(url);
    }
}