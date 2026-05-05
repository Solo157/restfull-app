package com.service.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

    Optional<Order> findOrderByUserId(String userId);

}
