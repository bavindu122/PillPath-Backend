package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PasswordResetToken;
import com.leo.pillpathbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing password reset tokens.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Find a token by its token string
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Find all tokens for a specific user
     */
    Optional<PasswordResetToken> findByUser(User user);
    
    /**
     * Delete all tokens for a specific user
     */
    void deleteByUser(User user);
    
    /**
     * Delete all expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Delete all used tokens older than a certain date (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.used = true AND t.createdAt < :cutoffDate")
    void deleteOldUsedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
}
