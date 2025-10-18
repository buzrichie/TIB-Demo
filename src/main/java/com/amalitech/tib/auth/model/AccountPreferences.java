package com.amalitech.tib.auth.model;

import com.amalitech.tib.shared.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "account_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountPreferences extends BaseEntity {



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private User user;

    @Column(name = "preferred_travel_class")
    private String preferredTravelClass;

    @ElementCollection
    @CollectionTable(name = "preferred_airlines", joinColumns = @JoinColumn(name = "account_preferences_id"))
    @Column(name = "airline")
    private List<String> preferredAirlines;

    @ElementCollection
    @CollectionTable(name = "preferred_hotel_chains", joinColumns = @JoinColumn(name = "account_preferences_id"))
    @Column(name = "hotel_chain")
    private List<String> preferredHotelChains;

    @Column(name = "meal_preference")
    private String mealPreference;

    @Column(name = "seat_preference")
    private String seatPreference;

    @Column(name = "branding_logo_url")
    private String brandingLogoUrl;

    @Column(name = "default_currency")
    private String defaultCurrency;

    @Column(name = "travel_policy", columnDefinition = "TEXT")
    private String travelPolicy;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

}
