package com.amalitech.tib.accommodation.model;

import com.amalitech.tib.booking.model.Booking;
import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.trip.model.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Table(name = "accommodation")
public class Accommodation extends BaseEntity {

    private String name;
    private String address;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal pricePerNight;
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @OneToMany(mappedBy = "accommodation")
    private Set<Booking> bookings;
}
