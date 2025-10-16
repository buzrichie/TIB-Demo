package com.amalitech.tib.activity.model;

import com.amalitech.tib.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;
import com.amalitech.tib.activity.enums.ActivityStatus;
import com.amalitech.tib.itinerary.model.Itinerary;
import com.amalitech.tib.attraction.model.Attraction;


@Entity
@Data
@Table(name = "activity")
public class Activity extends BaseEntity {

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    @Lob
    private String notes;
    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;
}
