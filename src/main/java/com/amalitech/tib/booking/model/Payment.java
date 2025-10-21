package com.amalitech.tib.booking.model;

import com.amalitech.tib.booking.enums.PaymentMethod;
import com.amalitech.tib.booking.enums.PaymentStatus;

import com.amalitech.tib.shared.util.BaseEntity;
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
