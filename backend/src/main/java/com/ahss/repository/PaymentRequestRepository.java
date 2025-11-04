package com.ahss.repository;

import com.ahss.entity.PaymentRequest;
import com.ahss.enums.PaymentRequestStatus;
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
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.requestCode = :requestCode")
    Optional<PaymentRequest> findByRequestCode(@Param("requestCode") String requestCode);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.paymentToken = :paymentToken")
    Optional<PaymentRequest> findByPaymentToken(@Param("paymentToken") String paymentToken);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = :status")
    List<PaymentRequest> findByStatus(@Param("status") PaymentRequestStatus status);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = :status")
    Page<PaymentRequest> findByStatus(@Param("status") PaymentRequestStatus status, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.payerEmail = :payerEmail")
    List<PaymentRequest> findByPayerEmail(@Param("payerEmail") String payerEmail);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.payerEmail = :payerEmail")
    Page<PaymentRequest> findByPayerEmail(@Param("payerEmail") String payerEmail, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.tenantId = :tenantId")
    Page<PaymentRequest> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.tenantId = :tenantId AND pr.status = :status")
    Page<PaymentRequest> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, 
                                                @Param("status") PaymentRequestStatus status, 
                                                Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.expiresAt < :now AND pr.status IN :statuses")
    List<PaymentRequest> findExpiredPaymentRequests(@Param("now") LocalDateTime now, 
                                                   @Param("statuses") List<PaymentRequestStatus> statuses);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentRequest> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.tenantId = :tenantId AND pr.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentRequest> findByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId,
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.amount BETWEEN :minAmount AND :maxAmount")
    List<PaymentRequest> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                            @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.currency = :currency")
    List<PaymentRequest> findByCurrency(@Param("currency") String currency);

    @Query("SELECT pr FROM PaymentRequest pr WHERE " +
           "LOWER(pr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(pr.payerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(pr.payerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR pr.requestCode LIKE CONCAT('%', :searchTerm, '%')")
    Page<PaymentRequest> searchPaymentRequests(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.tenantId = :tenantId AND (" +
           "LOWER(pr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(pr.payerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(pr.payerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR pr.requestCode LIKE CONCAT('%', :searchTerm, '%'))")
    Page<PaymentRequest> searchPaymentRequestsByTenant(@Param("tenantId") Long tenantId,
                                                      @Param("searchTerm") String searchTerm, 
                                                      Pageable pageable);

    @Query("SELECT COUNT(pr) > 0 FROM PaymentRequest pr WHERE pr.requestCode = :requestCode")
    boolean existsByRequestCode(@Param("requestCode") String requestCode);

    @Query("SELECT COUNT(pr) > 0 FROM PaymentRequest pr WHERE pr.paymentToken = :paymentToken")
    boolean existsByPaymentToken(@Param("paymentToken") String paymentToken);

    @Query("SELECT COUNT(pr) FROM PaymentRequest pr WHERE pr.status = :status")
    long countByStatus(@Param("status") PaymentRequestStatus status);

    @Query("SELECT COUNT(pr) FROM PaymentRequest pr WHERE pr.tenantId = :tenantId AND pr.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") PaymentRequestStatus status);

    @Query("SELECT SUM(pr.amount) FROM PaymentRequest pr WHERE pr.status = :status AND pr.currency = :currency")
    BigDecimal sumAmountByStatusAndCurrency(@Param("status") PaymentRequestStatus status, 
                                           @Param("currency") String currency);

    @Query("SELECT SUM(pr.amount) FROM PaymentRequest pr WHERE pr.tenantId = :tenantId AND pr.status = :status AND pr.currency = :currency")
    BigDecimal sumAmountByTenantIdAndStatusAndCurrency(@Param("tenantId") Long tenantId,
                                                      @Param("status") PaymentRequestStatus status, 
                                                      @Param("currency") String currency);

    @Query("SELECT pr FROM PaymentRequest pr LEFT JOIN FETCH pr.transactions WHERE pr.id = :id")
    Optional<PaymentRequest> findWithTransactions(@Param("id") UUID id);

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status IN :statuses ORDER BY pr.createdAt DESC")
    List<PaymentRequest> findRecentByStatuses(@Param("statuses") List<PaymentRequestStatus> statuses, Pageable pageable);
}