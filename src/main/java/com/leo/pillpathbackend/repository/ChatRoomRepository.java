package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customer.id = :customerId AND cr.pharmacy.id = :pharmacyId AND cr.isActive = true")
    Optional<ChatRoom> findActiveByCustomerIdAndPharmacyId(@Param("customerId") Long customerId, @Param("pharmacyId") Long pharmacyId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customer.id = :customerId ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.pharmacist.id = :pharmacistId ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findByPharmacistId(@Param("pharmacistId") Long pharmacistId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.pharmacy.id = :pharmacyId ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}

