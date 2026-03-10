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
public class BillingAdapterService implements BillingAdapter {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public boolean createAccount(String userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://billing-service/api/billing/account?userId=" + userId))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, "NoAuth ")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            // do nothing
            return false;
        }
    }

}
