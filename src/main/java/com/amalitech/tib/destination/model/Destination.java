package com.amalitech.tib.destination.model;

import com.amalitech.tib.attraction.model.Attraction;
import com.amalitech.tib.destination.enums.DestinationStatus;
import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.trip.model.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "destination")
public class Destination extends BaseEntity {

    private String name;
    private String country;
    private String region;
    private String imageUrl;
    private Float latitude;
    private Float longitude;
    @Lob
    private String description;
    @Enumerated(EnumType.STRING)
    private DestinationStatus status;

    @OneToMany(mappedBy = "destination")
    private Set<Trip> trips;

    @OneToMany(mappedBy = "destination")
    private Set<Attraction> attractions;
}
