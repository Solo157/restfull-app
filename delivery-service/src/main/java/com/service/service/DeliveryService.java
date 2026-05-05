package com.service.service;

import com.service.adapter.OrderItemDTO;
import com.service.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Главный сервис по работе с доставкой и курьерами.
 */
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final CourierRepository courierRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public List<Courier> getCouriers() {
        return courierRepository.findAll();
    }

    @Transactional
    public void addNewCourier() {
        Courier courier = new Courier();
        courier.setName(UUID.randomUUID() + "-name");
        courierRepository.save(courier);
    }

    /**
     * Назначить курьера на заказ.
     */
    @Transactional
    public boolean assignmentCourierToOrder(Long orderId, List<OrderItemDTO> itemDTOS, String address) {
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
        List<OrderItem> items = itemDTOS.stream()
                .map(item -> new OrderItem(item.getProductName(), item.getPrice(), item.getCount()))
                .toList();
        delivery.setItems(items);
        delivery.setAddress(address);

        deliveryRepository.save(delivery);
        courierRepository.save(waitingCourier);
        return true;
    }

    /**
     * Снять курьера с заказа.
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
