package com.project.service;

import com.project.domain.model.PaymentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentInvoiceRepository extends JpaRepository<PaymentInvoice, UUID> {
    List<PaymentInvoice> findByStatusAndExpiresAtBefore(PaymentInvoice.Status status, OffsetDateTime time);
}
