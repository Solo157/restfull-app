package com.service.adapters;

import com.service.database.OrderStatus;
import com.service.database.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusEventDTO {

    private String userId;
    private Long orderId;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private Long amount;

}
