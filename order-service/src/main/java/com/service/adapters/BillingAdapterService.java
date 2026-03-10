package com.service.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@RequiredArgsConstructor
public class BillingAdapterService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean checkAccountAmount(String userId, String orderId, Long orderAmount) {
        try {
            String uriStr = String.format(
                    "http://billing-service/api/billing/account/%s/amount?orderId=%s&orderAmount=%d",
                    userId, orderId, orderAmount
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uriStr))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, "NoAuth")
                    .GET() // Используем GET
                    .build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            } else {
                // Можно обработать другие статус-коды
                return false;
            }
        } catch (Exception e) {
            // логировать исключение
            e.printStackTrace();
            return false;
        }
    }

}
