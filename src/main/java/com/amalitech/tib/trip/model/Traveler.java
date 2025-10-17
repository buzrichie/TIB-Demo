package com.amalitech.tib.trip.model;

import com.amalitech.tib.booking.model.Booking;
import com.amalitech.tib.notification.model.Feedback;
import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.trip.enums.TravelerRole;
import com.amalitech.tib.authentication.user.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

/**
 * Entity representing a traveler participating in a trip.
 */
@Entity
@Data
@Table(name = "traveler")
public class Traveler extends BaseEntity {

    private String firstname;
    private String lastname;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private TravelerRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "traveler")
    private Set<Booking> bookings;

    @OneToMany(mappedBy = "traveler")
    private Set<Feedback> feedbackSubmissions;

}
