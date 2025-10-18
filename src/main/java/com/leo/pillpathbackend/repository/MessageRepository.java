package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.timestamp DESC")
    Page<Message> findByChatRoomIdOrderByTimestampDesc(@Param("chatRoomId") Long chatRoomId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.timestamp DESC")
    List<Message> findByChatRoomIdOrderByTimestampDesc(@Param("chatRoomId") Long chatRoomId);

    @Query(value = "SELECT m FROM Message m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.timestamp DESC")
    List<Message> findTopByChatRoomIdOrderByTimestampDesc(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
}

