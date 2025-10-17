package com.amalitech.tib.itinerary.model;

import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.itinerary.enums.TransportType;
import com.amalitech.tib.trip.model.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transport")
public class Transport extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransportType type;
    private String provider;
    private String departure;
    private String arrival;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;
}
