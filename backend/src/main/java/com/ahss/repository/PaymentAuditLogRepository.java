package com.ahss.repository;

import com.ahss.entity.PaymentAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, UUID> {

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.paymentRequestId = :paymentRequestId ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByPaymentRequestIdOrderByCreatedAtDesc(@Param("paymentRequestId") UUID paymentRequestId);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.paymentRequestId = :paymentRequestId ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findByPaymentRequestIdOrderByCreatedAtDesc(@Param("paymentRequestId") UUID paymentRequestId, Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.paymentTransactionId = :paymentTransactionId ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByPaymentTransactionIdOrderByCreatedAtDesc(@Param("paymentTransactionId") UUID paymentTransactionId);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.paymentRefundId = :paymentRefundId ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByPaymentRefundIdOrderByCreatedAtDesc(@Param("paymentRefundId") UUID paymentRefundId);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.action = :action ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByActionOrderByCreatedAtDesc(@Param("action") String action);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.action = :action ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findByActionOrderByCreatedAtDesc(@Param("action") String action, Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.userId = :userId ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.userId = :userId ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.userId IS NULL ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findSystemActionsOrderByCreatedAtDesc();

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.userId IS NULL ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findSystemActionsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.userId IS NOT NULL ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findUserActionsOrderByCreatedAtDesc();

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.userId IS NOT NULL ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findUserActionsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.createdAt BETWEEN :startDate AND :endDate ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, 
                                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.createdAt BETWEEN :startDate AND :endDate ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, 
                                                                    @Param("endDate") LocalDateTime endDate, 
                                                                    Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.oldStatus != pal.newStatus ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findStatusChangesOrderByCreatedAtDesc();

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.oldStatus != pal.newStatus ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> findStatusChangesOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.oldStatus = :oldStatus AND pal.newStatus = :newStatus ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByStatusChangeOrderByCreatedAtDesc(@Param("oldStatus") String oldStatus, 
                                                                @Param("newStatus") String newStatus);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.ipAddress = :ipAddress ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByIpAddressOrderByCreatedAtDesc(@Param("ipAddress") String ipAddress);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE " +
           "pal.action LIKE CONCAT('%', :searchTerm, '%') " +
           "OR LOWER(pal.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR pal.oldStatus LIKE CONCAT('%', :searchTerm, '%') " +
           "OR pal.newStatus LIKE CONCAT('%', :searchTerm, '%') " +
           "ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> searchAuditLogs(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.paymentRequestId = :paymentRequestId AND " +
           "(pal.action LIKE CONCAT('%', :searchTerm, '%') " +
           "OR LOWER(pal.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR pal.oldStatus LIKE CONCAT('%', :searchTerm, '%') " +
           "OR pal.newStatus LIKE CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY pal.createdAt DESC")
    Page<PaymentAuditLog> searchAuditLogsByPaymentRequest(@Param("paymentRequestId") UUID paymentRequestId,
                                                         @Param("searchTerm") String searchTerm, 
                                                         Pageable pageable);

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.paymentRequestId = :paymentRequestId")
    long countByPaymentRequestId(@Param("paymentRequestId") UUID paymentRequestId);

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.paymentTransactionId = :paymentTransactionId")
    long countByPaymentTransactionId(@Param("paymentTransactionId") UUID paymentTransactionId);

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.paymentRefundId = :paymentRefundId")
    long countByPaymentRefundId(@Param("paymentRefundId") UUID paymentRefundId);

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.action = :action")
    long countByAction(@Param("action") String action);

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.userId IS NULL")
    long countSystemActions();

    @Query("SELECT COUNT(pal) FROM PaymentAuditLog pal WHERE pal.userId IS NOT NULL")
    long countUserActions();

    @Query("SELECT pal FROM PaymentAuditLog pal LEFT JOIN FETCH pal.user WHERE pal.id = :id")
    PaymentAuditLog findWithUser(@Param("id") Long id);

    @Query("SELECT pal FROM PaymentAuditLog pal LEFT JOIN FETCH pal.paymentRequest WHERE pal.id = :id")
    PaymentAuditLog findWithPaymentRequest(@Param("id") Long id);

    @Query("SELECT pal FROM PaymentAuditLog pal LEFT JOIN FETCH pal.paymentTransaction WHERE pal.id = :id")
    PaymentAuditLog findWithPaymentTransaction(@Param("id") Long id);

    @Query("SELECT pal FROM PaymentAuditLog pal LEFT JOIN FETCH pal.paymentRefund WHERE pal.id = :id")
    PaymentAuditLog findWithPaymentRefund(@Param("id") Long id);

    @Query("SELECT DISTINCT pal.action FROM PaymentAuditLog pal ORDER BY pal.action")
    List<String> findDistinctActions();

    @Query("SELECT pal FROM PaymentAuditLog pal WHERE pal.paymentRequestId IN :paymentRequestIds ORDER BY pal.createdAt DESC")
    List<PaymentAuditLog> findByPaymentRequestIdsOrderByCreatedAtDesc(@Param("paymentRequestIds") List<UUID> paymentRequestIds);
}