package com.service.api;

import lombok.Data;

@Data
public class InventoryRequest {

    private String productName;
    private Integer count;

}
