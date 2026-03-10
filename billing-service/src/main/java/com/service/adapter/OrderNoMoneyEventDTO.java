package com.service.adapter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderNoMoneyEventDTO {

    private String userId;
    private String orderId;
    private String orderAmount;

}
