package com.service.mapper;

import com.service.api.dto.OrderDTO;
import com.service.database.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    Order toOrder(OrderDTO orderDTO);

    OrderDTO toOrderDTO(Order order);

}
