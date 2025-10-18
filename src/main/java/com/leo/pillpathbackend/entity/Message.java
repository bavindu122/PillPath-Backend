package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a single message in a chat room.
 * Messages are sent by either customers or pharmacists.
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_chat_room_timestamp", columnList = "chat_room_id,timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The chat room this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * The user who sent this message (can be Customer or PharmacistUser)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The actual message content
     */
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    /**
     * Flag to indicate if the message has been read
     */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /**
     * Timestamp when the message was read
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Message type (TEXT, IMAGE, FILE, etc.)
     */
    @Column(name = "message_type")
    private String messageType = "TEXT";

    /**
     * Optional: URL for media messages (images, files)
     */
    @Column(name = "media_url")
    private String mediaUrl;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * Mark the message as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
