package com.service.service;

import com.service.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Главный сервис по работе с биллингом.
 */
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final CourierRepository courierRepository;
    private final DeliveryRepository deliveryRepository;

    public List<Courier> getCouriers() {
        return courierRepository.findAll();
    }

    @Transactional
    public void addNewCourier() {
        courierRepository.save(new Courier());
    }

    /**
     * Положить деньги на аккаунт.
     */
    @Transactional
    public boolean assignmentCourierToOrder(Long orderId, List<OrderItem> items, String address) {
        List<Courier> waitingCouriers = courierRepository.findAll().stream()
                .filter(courier -> courier.getStatus() == CourierStatus.WAITING)
                .toList();

        if (waitingCouriers.isEmpty()) {
            return false;
        }

        Courier waitingCourier = waitingCouriers.stream().findFirst().get();
        waitingCourier.setStatus(CourierStatus.IN_PROCESS);
        Delivery delivery = new Delivery();
        delivery.setCourierId(waitingCourier.getId());
        delivery.setOrderId(orderId);
        delivery.setItems(items);
        delivery.setAddress(address);

        deliveryRepository.save(delivery);
        courierRepository.save(waitingCourier);
        return true;
    }

    /**
     * Снять деньги с аккаунта.
     */
    @Transactional
    public boolean unassignmentCourierToOrder(Long orderId) {
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(orderId);
        if (deliveryOpt.isEmpty()) {
            return false;
        }

        Delivery delivery = deliveryOpt.get();
        Long courierId = delivery.getCourierId();

        deliveryRepository.delete(delivery);

        Optional<Courier> courierOpt = courierRepository.findById(courierId);
        if (courierOpt.isEmpty()) {
            return false;
        }

        Courier courier = courierOpt.get();
        courier.setStatus(CourierStatus.WAITING);
        courierRepository.save(courier);

        return true;
    }

}
