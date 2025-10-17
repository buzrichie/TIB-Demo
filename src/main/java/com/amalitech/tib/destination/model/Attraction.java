package com.amalitech.tib.destination.model;

import com.amalitech.tib.shared.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import com.amalitech.tib.destination.enums.AttractionStatus;
import com.amalitech.tib.itinerary.model.Activity;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
@Table(name = "attraction")
public class Attraction extends BaseEntity {

    private String name;
    @Lob
    private String description;
    private String address;
    private Float latitude;
    private Float longitude;
    private BigDecimal price;
    private String priceDescription;
    private String imageUrl;
    private String websiteUrl;
    @Enumerated(EnumType.STRING)
    private AttractionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "attraction")
    private Set<Activity> activities;
}
