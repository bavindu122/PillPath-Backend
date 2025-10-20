package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a chat room between a customer and a pharmacist.
 * Each chat room is associated with a specific pharmacy and initiated by a customer.
 */
@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The customer who initiated the chat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * The pharmacist assigned to this chat (can be null initially)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private PharmacistUser pharmacist;

    /**
     * The pharmacy this chat is associated with
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    /**
     * Flag to indicate if the chat is currently active
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Last message timestamp for sorting and display
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * Count of unread messages for customer
     */
    @Column(name = "unread_count_customer")
    private Integer unreadCountCustomer = 0;

    /**
     * Count of unread messages for pharmacist
     */
    @Column(name = "unread_count_pharmacist")
    private Integer unreadCountPharmacist = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Messages in this chat room
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    /**
     * Update the last message timestamp
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * Increment unread count for the recipient
     */
    public void incrementUnreadCount(boolean isCustomerSender) {
        if (isCustomerSender) {
            this.unreadCountPharmacist++;
        } else {
            this.unreadCountCustomer++;
        }
    }

    /**
     * Reset unread count for a specific user type
     */
    public void resetUnreadCount(boolean isCustomer) {
        if (isCustomer) {
            this.unreadCountCustomer = 0;
        } else {
            this.unreadCountPharmacist = 0;
        }
    }
}

