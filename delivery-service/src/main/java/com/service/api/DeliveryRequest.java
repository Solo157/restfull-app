package com.service.api;

import com.service.database.OrderItem;
import lombok.Data;

import java.util.*;

@Data
public class DeliveryRequest {

    private Long orderId;
    private List<OrderItem> items;
    private String address;

}
