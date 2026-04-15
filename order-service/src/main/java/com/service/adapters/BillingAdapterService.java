package com.service.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Севрис для отдельного межсервисного REST-запроса к биллинг сервису.
 */
@Service
@RequiredArgsConstructor
public class BillingAdapterService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${services.external.billing.port:8090}")
    private final String billingServicePort;

    /**
     * Проверка, хватит ли средств на аккаунте для оплаты заказа.
     */
    public boolean checkAccountAmount(String userId, String orderId, int orderAmount) {
        try {
            String uriString = String.format(
                    "http://billing-service-service:%s/billing/account/%s/amount?orderId=%s&orderAmount=%d",
                    billingServicePort, userId, orderId, orderAmount
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uriString))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .GET()
                    .build();

            System.out.println("URL: " + uriString);

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
