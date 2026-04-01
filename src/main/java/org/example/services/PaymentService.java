package org.example.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private final String PAYSTACK_SECRET = "YOUR_PAYSTACK_SECRET_KEY";

    public String initializePayment(String email, double amount) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.paystack.co/transaction/initialize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(PAYSTACK_SECRET);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("amount", (int)(amount * 100)); // kobo (important!)

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                request,
                Map.class
        );

        Map data = (Map) response.getBody().get("data");

        return data.get("authorization_url").toString();
    }
}