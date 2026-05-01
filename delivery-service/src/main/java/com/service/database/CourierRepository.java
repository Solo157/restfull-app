package com.service.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {

//    Optional<Courier> findById(@NonNull Long id);

}
