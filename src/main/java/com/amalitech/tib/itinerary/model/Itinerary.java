package com.amalitech.tib.itinerary.model;

import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.activity.model.Activity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Table(name = "itinerary")
public class Itinerary extends BaseEntity {

    private String title;
    private Integer dayNumber;
    private LocalDate date;
    @Lob
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @OneToMany(mappedBy = "itinerary")
    private Set<Activity> activities;
}
