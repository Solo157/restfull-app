package com.service.api;

import lombok.Data;

@Data
public class AccountRequest {

    private String userId;
    private Long amount;

}
