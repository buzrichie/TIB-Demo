package com.amalitech.tib.payment.model;

import com.amalitech.tib.booking.model.Booking;
import com.amalitech.tib.payment.enums.PaymentMethod;
import com.amalitech.tib.payment.enums.PaymentStatus;

import com.amalitech.tib.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "payment")
public class Payment extends BaseEntity {

    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paidAt;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Booking booking;
}
