package com.service.api;

import com.service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер биллинга. В этом контроллере все запросы требуют прохождения аутентификации, которая реализована через
 * сервис auth-сервис.
 */
@RestController
@RequestMapping("/api/delivery/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final DeliveryService deliveryService;

    /**
     * Получить аккаунт.
     */
    @PostMapping("/delivery/courier")
    public ResponseEntity<String> addNewCourier() {
        deliveryService.addNewCourier();
        return ResponseEntity.status(HttpStatus.OK).body("courier created");
    }

    /**
     * Увеличить баланс аккаунта.
     */
    @PostMapping("/delivery/assignment_courier")
    public ResponseEntity<String> assignmentCourierToOrder(@RequestBody DeliveryRequest request) {
        boolean assignment = deliveryService.assignmentCourierToOrder(request.getOrderId(), request.getItems(), request.getAddress());
        if (assignment) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not assigment");
    }



    /**
     * Уменьшить баланс аккаунта.
     */
    @PostMapping("/delivery/unassignment_courier")
    public ResponseEntity<String> unassignmentCourierToOrder(@RequestParam Long orderId) {
        boolean unassignment = deliveryService.unassignmentCourierToOrder(orderId);
        if (unassignment) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not unassigment");
    }

}
