package com.service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateDTO {

    /**
     * Адрес доставки.
     */
    private String deliveryAddress;
    /**
     * Контактный телефон.
     */
    private String contactPhone;

}
