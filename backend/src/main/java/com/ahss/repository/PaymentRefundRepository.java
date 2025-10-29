package com.ahss.repository;

import com.ahss.entity.PaymentRefund;
import com.ahss.enums.PaymentTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, UUID> {

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundCode = :refundCode")
    Optional<PaymentRefund> findByRefundCode(@Param("refundCode") String refundCode);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.externalRefundId = :externalRefundId")
    Optional<PaymentRefund> findByExternalRefundId(@Param("externalRefundId") String externalRefundId);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.paymentTransactionId = :paymentTransactionId")
    List<PaymentRefund> findByPaymentTransactionId(@Param("paymentTransactionId") UUID paymentTransactionId);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.paymentTransactionId = :paymentTransactionId ORDER BY pr.createdAt DESC")
    List<PaymentRefund> findByPaymentTransactionIdOrderByCreatedAtDesc(@Param("paymentTransactionId") UUID paymentTransactionId);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundStatus = :status")
    List<PaymentRefund> findByRefundStatus(@Param("status") PaymentTransactionStatus status);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundStatus = :status")
    Page<PaymentRefund> findByRefundStatus(@Param("status") PaymentTransactionStatus status, Pageable pageable);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.gatewayName = :gatewayName")
    List<PaymentRefund> findByGatewayName(@Param("gatewayName") String gatewayName);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.gatewayName = :gatewayName AND pr.refundStatus = :status")
    List<PaymentRefund> findByGatewayNameAndStatus(@Param("gatewayName") String gatewayName, 
                                                  @Param("status") PaymentTransactionStatus status);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentRefund> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.processedAt BETWEEN :startDate AND :endDate")
    List<PaymentRefund> findByProcessedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundAmount BETWEEN :minAmount AND :maxAmount")
    List<PaymentRefund> findByRefundAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                                 @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.currency = :currency")
    List<PaymentRefund> findByCurrency(@Param("currency") String currency);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.errorCode = :errorCode")
    List<PaymentRefund> findByErrorCode(@Param("errorCode") String errorCode);

    @Query("SELECT pr FROM PaymentRefund pr WHERE LOWER(pr.reason) LIKE LOWER(CONCAT('%', :reason, '%'))")
    List<PaymentRefund> findByReasonContaining(@Param("reason") String reason);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundStatus = :status AND pr.createdAt < :cutoffTime")
    List<PaymentRefund> findStaleRefunds(@Param("status") PaymentTransactionStatus status, 
                                        @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT pr FROM PaymentRefund pr WHERE " +
           "pr.refundCode LIKE CONCAT('%', :searchTerm, '%') " +
           "OR pr.externalRefundId LIKE CONCAT('%', :searchTerm, '%') " +
           "OR pr.gatewayName LIKE CONCAT('%', :searchTerm, '%') " +
           "OR LOWER(pr.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<PaymentRefund> searchRefunds(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(pr) > 0 FROM PaymentRefund pr WHERE pr.refundCode = :refundCode")
    boolean existsByRefundCode(@Param("refundCode") String refundCode);

    @Query("SELECT COUNT(pr) > 0 FROM PaymentRefund pr WHERE pr.externalRefundId = :externalRefundId")
    boolean existsByExternalRefundId(@Param("externalRefundId") String externalRefundId);

    @Query("SELECT COUNT(pr) FROM PaymentRefund pr WHERE pr.refundStatus = :status")
    long countByRefundStatus(@Param("status") PaymentTransactionStatus status);

    @Query("SELECT COUNT(pr) FROM PaymentRefund pr WHERE pr.paymentTransactionId = :paymentTransactionId")
    long countByPaymentTransactionId(@Param("paymentTransactionId") UUID paymentTransactionId);

    @Query("SELECT SUM(pr.refundAmount) FROM PaymentRefund pr WHERE pr.refundStatus = :status AND pr.currency = :currency")
    BigDecimal sumRefundAmountByStatusAndCurrency(@Param("status") PaymentTransactionStatus status, 
                                                 @Param("currency") String currency);

    @Query("SELECT SUM(pr.refundAmount) FROM PaymentRefund pr WHERE pr.paymentTransactionId = :paymentTransactionId AND pr.refundStatus = :status")
    BigDecimal sumRefundAmountByTransactionIdAndStatus(@Param("paymentTransactionId") UUID paymentTransactionId,
                                                      @Param("status") PaymentTransactionStatus status);

    @Query("SELECT pr FROM PaymentRefund pr LEFT JOIN FETCH pr.paymentTransaction WHERE pr.id = :id")
    Optional<PaymentRefund> findWithPaymentTransaction(@Param("id") UUID id);

    @Query("SELECT pr FROM PaymentRefund pr LEFT JOIN FETCH pr.paymentTransaction pt LEFT JOIN FETCH pt.paymentRequest WHERE pr.id = :id")
    Optional<PaymentRefund> findWithPaymentTransactionAndRequest(@Param("id") UUID id);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundStatus = 'SUCCESS' AND pr.paymentTransactionId = :paymentTransactionId")
    List<PaymentRefund> findSuccessfulRefundsByTransactionId(@Param("paymentTransactionId") UUID paymentTransactionId);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.refundStatus IN :statuses ORDER BY pr.createdAt DESC")
    List<PaymentRefund> findRecentByStatuses(@Param("statuses") List<PaymentTransactionStatus> statuses, Pageable pageable);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.createdBy = :userId")
    List<PaymentRefund> findByCreatedBy(@Param("userId") Long userId);

    @Query("SELECT pr FROM PaymentRefund pr WHERE pr.createdBy = :userId")
    Page<PaymentRefund> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
}