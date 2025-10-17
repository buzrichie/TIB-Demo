package com.amalitech.tib.category.model;

import com.amalitech.tib.destination.model.Attraction;
import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "category")
public class Category extends BaseEntity {
    private String name;
    @Lob
    private String description;

    @OneToMany(mappedBy = "category")
    private Set<Attraction> attractions;
}
