package com.service.api;

import com.service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер сервиса доставки. В этом контроллере все запросы требуют прохождения аутентификации, которая реализована через
 * сервис auth-сервис.
 */
@RestController
@RequestMapping("/api/delivery/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final DeliveryService deliveryService;

    /**
     * Получить нового курьера.
     */
    @PostMapping("/delivery/courier")
    public ResponseEntity<String> addNewCourier() {
        deliveryService.addNewCourier();
        return ResponseEntity.status(HttpStatus.OK).body("courier created");
    }

}
