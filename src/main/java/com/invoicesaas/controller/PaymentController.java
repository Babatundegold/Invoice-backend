package com.invoicesaas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.invoicesaas.dto.PaymentDto;
import com.invoicesaas.security.CurrentUser;
import com.invoicesaas.service.PaystackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaystackService paystackService;

    // Called from the PUBLIC invoice page - client initiates payment for their invoice
    @PostMapping("/init")
    public ResponseEntity<PaymentDto.InitResponse> initPayment(@RequestBody PaymentDto.InitRequest request) {
        return ResponseEntity.ok(paystackService.initInvoicePayment(request.getInvoiceId()));
    }

    // Business owner manually marks an invoice as paid (cash / bank transfer)
    @PostMapping("/manual")
    public ResponseEntity<Void> recordManualPayment(@RequestBody PaymentDto.ManualPaymentRequest request) {
        paystackService.recordManualPayment(CurrentUser.id(), request.getInvoiceId(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    // Premium subscription checkout
    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe() {
        return ResponseEntity.ok(paystackService.initSubscription(CurrentUser.id()));
    }

    /**
     * Paystack webhook - configure this URL (https://yourdomain.com/api/payments/webhook)
     * in your Paystack dashboard. Verifies event type and updates invoice/subscription status.
     * NOTE: in production, verify the x-paystack-signature header against your secret key
     * before trusting the payload (left as a TODO with a comment below).
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody JsonNode payload,
                                         @RequestHeader(value = "x-paystack-signature", required = false) String signature) {
        // TODO: verify `signature` is HMAC-SHA512 of the raw payload using your Paystack secret key.
        // Skipping this in production will let anyone forge "successful payment" events.

        String event = payload.get("event").asText();
        JsonNode data = payload.get("data");

        if ("charge.success".equals(event)) {
            String reference = data.get("reference").asText();
            paystackService.handleChargeSuccess(reference);
        }
        // subscription.create / invoice.payment_failed / subscription.disable events
        // can be handled similarly here for the premium subscription lifecycle.

        return ResponseEntity.ok().build();
    }
}
