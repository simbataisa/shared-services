package com.ahss.repository;

import com.ahss.entity.PaymentTransaction;
import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.enums.PaymentTransactionType;
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
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionCode = :transactionCode")
    Optional<PaymentTransaction> findByTransactionCode(@Param("transactionCode") String transactionCode);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.externalTransactionId = :externalTransactionId")
    Optional<PaymentTransaction> findByExternalTransactionId(@Param("externalTransactionId") String externalTransactionId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentRequestId = :paymentRequestId")
    List<PaymentTransaction> findByPaymentRequestId(@Param("paymentRequestId") UUID paymentRequestId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentRequestId = :paymentRequestId ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByPaymentRequestIdOrderByCreatedAtDesc(@Param("paymentRequestId") UUID paymentRequestId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionStatus = :status")
    List<PaymentTransaction> findByTransactionStatus(@Param("status") PaymentTransactionStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionStatus = :status")
    Page<PaymentTransaction> findByTransactionStatus(@Param("status") PaymentTransactionStatus status, Pageable pageable);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionType = :type")
    List<PaymentTransaction> findByTransactionType(@Param("type") PaymentTransactionType type);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionType = :type AND pt.transactionStatus = :status")
    List<PaymentTransaction> findByTransactionTypeAndStatus(@Param("type") PaymentTransactionType type, 
                                                           @Param("status") PaymentTransactionStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentMethod = :paymentMethod")
    List<PaymentTransaction> findByPaymentMethod(@Param("paymentMethod") PaymentMethodType paymentMethod);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.gatewayName = :gatewayName")
    List<PaymentTransaction> findByGatewayName(@Param("gatewayName") String gatewayName);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.gatewayName = :gatewayName AND pt.transactionStatus = :status")
    List<PaymentTransaction> findByGatewayNameAndStatus(@Param("gatewayName") String gatewayName, 
                                                       @Param("status") PaymentTransactionStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.processedAt BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findByProcessedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.amount BETWEEN :minAmount AND :maxAmount")
    List<PaymentTransaction> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                                @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.currency = :currency")
    List<PaymentTransaction> findByCurrency(@Param("currency") String currency);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.errorCode = :errorCode")
    List<PaymentTransaction> findByErrorCode(@Param("errorCode") String errorCode);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionStatus = :status AND pt.createdAt < :cutoffTime")
    List<PaymentTransaction> findStaleTransactions(@Param("status") PaymentTransactionStatus status, 
                                                  @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentRequestId = :paymentRequestId AND pt.transactionType = :type AND pt.transactionStatus = :status")
    List<PaymentTransaction> findByPaymentRequestIdAndTypeAndStatus(@Param("paymentRequestId") UUID paymentRequestId,
                                                                   @Param("type") PaymentTransactionType type,
                                                                   @Param("status") PaymentTransactionStatus status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE " +
           "pt.transactionCode LIKE CONCAT('%', :searchTerm, '%') " +
           "OR pt.externalTransactionId LIKE CONCAT('%', :searchTerm, '%') " +
           "OR pt.gatewayName LIKE CONCAT('%', :searchTerm, '%')")
    Page<PaymentTransaction> searchTransactions(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(pt) > 0 FROM PaymentTransaction pt WHERE pt.transactionCode = :transactionCode")
    boolean existsByTransactionCode(@Param("transactionCode") String transactionCode);

    @Query("SELECT COUNT(pt) > 0 FROM PaymentTransaction pt WHERE pt.externalTransactionId = :externalTransactionId")
    boolean existsByExternalTransactionId(@Param("externalTransactionId") String externalTransactionId);

    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.transactionStatus = :status")
    long countByTransactionStatus(@Param("status") PaymentTransactionStatus status);

    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.transactionType = :type")
    long countByTransactionType(@Param("type") PaymentTransactionType type);

    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.paymentMethod = :paymentMethod")
    long countByPaymentMethod(@Param("paymentMethod") PaymentMethodType paymentMethod);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.transactionStatus = :status AND pt.currency = :currency")
    BigDecimal sumAmountByStatusAndCurrency(@Param("status") PaymentTransactionStatus status, 
                                           @Param("currency") String currency);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.transactionType = :type AND pt.transactionStatus = :status AND pt.currency = :currency")
    BigDecimal sumAmountByTypeAndStatusAndCurrency(@Param("type") PaymentTransactionType type,
                                                  @Param("status") PaymentTransactionStatus status, 
                                                  @Param("currency") String currency);

    @Query("SELECT pt FROM PaymentTransaction pt LEFT JOIN FETCH pt.paymentRequest WHERE pt.id = :id")
    Optional<PaymentTransaction> findWithPaymentRequest(@Param("id") UUID id);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentRequestId = :paymentRequestId AND pt.transactionType = 'PAYMENT' AND pt.transactionStatus = 'SUCCESS'")
    List<PaymentTransaction> findSuccessfulPaymentsByRequestId(@Param("paymentRequestId") UUID paymentRequestId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionStatus IN :statuses ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findRecentByStatuses(@Param("statuses") List<PaymentTransactionStatus> statuses, Pageable pageable);
}