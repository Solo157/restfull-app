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
 * Адаптер для доступа к биллингому сервису.
 */
@Service
@RequiredArgsConstructor
public class BillingAdapterService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${services.external.billing.port:8090}")
    private final String billingServicePort;

    /**
     * Создать аккаунт в биллинге.
     */
    public boolean createBillingAccount(String userId) {
        try {
            String uriString = String.format("http://billing-service-service:%s/billing/account?userId=%s", billingServicePort, userId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uriString))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

}
