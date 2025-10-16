package com.amalitech.tib.booking.model;

import com.amalitech.tib.accommodation.model.Accommodation;
import com.amalitech.tib.payment.model.Payment;
import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.traveler.model.Traveler;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "booking")
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id")
    private Traveler traveler;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
