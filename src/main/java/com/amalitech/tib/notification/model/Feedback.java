package com.amalitech.tib.feedback.model;

import com.amalitech.tib.util.BaseEntity;
import com.amalitech.tib.traveler.model.Traveler;
import com.amalitech.tib.trip.model.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "feedback")
public class Feedback extends BaseEntity {

    private Integer rating;
    @Lob
    private String comments;
    private Instant submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id")
    private Traveler traveler;
}
