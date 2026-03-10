package com.service.api;

import lombok.Data;

@Data
public class OrderRequest {

    private String userId;
    private Long amount;

}
