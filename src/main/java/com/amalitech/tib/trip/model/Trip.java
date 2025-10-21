package com.amalitech.tib.trip.model;

import com.amalitech.tib.booking.model.Accommodation;
import com.amalitech.tib.budget.model.Budget;
import com.amalitech.tib.destination.model.Destination;
import com.amalitech.tib.itinerary.model.Itinerary;
import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.itinerary.model.Transport;
import com.amalitech.tib.trip.enums.TripStatus;
import com.amalitech.tib.auth.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Entity representing a travel plan containing schedule, budget and related resources.
 */
@Entity
@Data
@Table(name = "trip")
public class Trip extends BaseEntity {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @OneToMany(mappedBy = "trip")
    private Set<Traveler> travelers;

    @OneToMany(mappedBy = "trip")
    private Set<Itinerary> itineraries;

    @OneToMany(mappedBy = "trip")
    private Set<Accommodation> accommodations;

    @OneToMany(mappedBy = "trip")
    private Set<Transport> transports;

    @OneToOne(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Budget tripBudget;
}
