package com.amalitech.tib.booking.model;

import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.trip.model.Traveler;

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
