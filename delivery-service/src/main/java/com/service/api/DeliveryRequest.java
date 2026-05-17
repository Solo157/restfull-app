package com.service.api;

import com.service.adapter.OrderItemDTO;
import com.service.database.OrderItem;
import lombok.Data;

import java.util.*;

@Data
public class DeliveryRequest {

    private Long orderId;
    private List<OrderItemDTO> items;
    private String address;

}
