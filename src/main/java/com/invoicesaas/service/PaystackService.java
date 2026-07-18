package com.invoicesaas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoicesaas.dto.PaymentDto;
import com.invoicesaas.entity.*;
import com.invoicesaas.exception.BadRequestException;
import com.invoicesaas.exception.ResourceNotFoundException;
import com.invoicesaas.repository.InvoiceRepository;
import com.invoicesaas.repository.PaymentRepository;
import com.invoicesaas.repository.SubscriptionRepository;
import com.invoicesaas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaystackService {

    @Value("${app.paystack.secret-key}")
    private String secretKey;

    @Value("${app.paystack.base-url}")
    private String paystackBaseUrl;

    @Value("${app.paystack.premium-monthly-plan-code}")
    private String premiumPlanCode;

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LedgerService ledgerService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebClient client() {
        return webClientBuilder
                .baseUrl(paystackBaseUrl)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /** Initialize a one-time payment for a client paying an invoice. */
    public PaymentDto.InitResponse initInvoicePayment(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getClient().getEmail() == null) {
            throw new BadRequestException("Client must have an email to pay via Paystack");
        }

        BigDecimal amountRemaining = invoice.getTotal().subtract(invoice.getAmountPaid());
        String reference = "INV-PAY-" + UUID.randomUUID().toString().substring(0, 12);

        Map<String, Object> body = Map.of(
                "email", invoice.getClient().getEmail(),
                "amount", amountRemaining.multiply(BigDecimal.valueOf(100)).intValue(), // kobo
                "reference", reference,
                "metadata", Map.of("invoiceId", invoice.getId())
        );

        JsonNode response = client().post()
                .uri("/transaction/initialize")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        JsonNode data = response.get("data");

        Payment payment = Payment.builder()
                .invoice(invoice)
                .paystackReference(reference)
                .amount(amountRemaining)
                .status("PENDING")
                .channel("paystack")
                .build();
        paymentRepository.save(payment);

        PaymentDto.InitResponse result = new PaymentDto.InitResponse();
        result.setAuthorizationUrl(data.get("authorization_url").asText());
        result.setAccessCode(data.get("access_code").asText());
        result.setReference(reference);
        return result;
    }

    /** Called by the Paystack webhook when a charge.success event fires. */
    public void handleChargeSuccess(String reference) {
        Payment payment = paymentRepository.findByPaystackReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for reference " + reference));

        if ("SUCCESS".equals(payment.getStatus())) return; // idempotency guard

        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        Invoice invoice = payment.getInvoice();
        invoice.setAmountPaid(invoice.getAmountPaid().add(payment.getAmount()));

        if (invoice.getAmountPaid().compareTo(invoice.getTotal()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        ledgerService.recordPaymentReceived(invoice, payment.getAmount(), "paystack");
    }

    /** Record a manually-marked payment (cash/bank transfer) rather than through Paystack. */
    public void recordManualPayment(Long ownerUserId, Long invoiceId, BigDecimal amount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        invoice.setAmountPaid(invoice.getAmountPaid().add(amount));
        if (invoice.getAmountPaid().compareTo(invoice.getTotal()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(amount)
                .status("SUCCESS")
                .channel("manual")
                .build();
        paymentRepository.save(payment);

        ledgerService.recordPaymentReceived(invoice, amount, "manual");
    }

    /** Initialize a recurring Premium subscription charge for the SaaS itself. */
    public String initSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> body = Map.of(
                "email", user.getEmail(),
                "plan", premiumPlanCode,
                "amount", 0 // amount is derived from the plan on Paystack's side
        );

        JsonNode response = client().post()
                .uri("/transaction/initialize")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.get("data").get("authorization_url").asText();
    }

    /** Called by webhook on subscription.create / charge.success for a subscription-linked transaction. */
    public void activatePremium(Long userId, String subscriptionCode, String customerCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPremium(true);
        userRepository.save(user);

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElse(Subscription.builder().user(user).createdAt(LocalDateTime.now()).build());
        subscription.setPaystackSubscriptionCode(subscriptionCode);
        subscription.setPaystackCustomerCode(customerCode);
        subscription.setPlanCode(premiumPlanCode);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
    }

    public void cancelPremium(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPremium(false);
        userRepository.save(user);

        subscriptionRepository.findByUserId(userId).ifPresent(sub -> {
            sub.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(sub);
        });
    }
}
