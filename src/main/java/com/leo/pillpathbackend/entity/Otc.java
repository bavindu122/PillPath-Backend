package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;



    @Entity
    @Table(name="otc")
    @Getter
    @Setter
    @AllArgsConstructor


    public class Otc {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String description;

        @Column(nullable = false)
        private Double price;

        @Column(nullable = false)
        private Integer stock;

        @Column(nullable = true, columnDefinition = "LONGTEXT")
        private String imageUrl;

        @Column(length = 50)
        private String status;

        @Column(name = "added_to_store")
        private Boolean addedToStore = true;


        @CreationTimestamp
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        public Otc() {}

        // Constructor with parameters
        public Otc(String name, String description, Double price, Integer stock, String imageUrl) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.stock = stock;
            this.imageUrl = imageUrl;
            this.addedToStore = true; // New products are added to store by default
            this.status = calculateStatus(stock);
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        // Method to automatically calculate status based on stock
        private String calculateStatus(Integer stock) {
            if (stock == 0) {
                return "Out of Stock";
            } else if (stock <= 10) {
                return "Low Stock";
            } else {
                return "In Stock";
            }
        }

        // Helper method to add product to store
        public void addToStore() {
            this.addedToStore = true;
        }

        // Helper method to remove product from store
        public void removeFromStore() {
            this.addedToStore = false;
        }

        // Setter for stock that also updates status
        public void setStock(Integer stock) {
            this.stock = stock;
            this.status = calculateStatus(stock);
        }

        // This method runs before saving to database
        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
            if (status == null && stock != null) {
                status = calculateStatus(stock);
            }
            if (this.addedToStore == null) {
                this.addedToStore = false;
            }


        }


        // This method runs before updating in database
        @PreUpdate
        protected void onUpdate() {
            updatedAt = LocalDateTime.now();
            if (stock != null) {
                status = calculateStatus(stock);
            }
        }




    }



