package com.amalitech.tib.budget.model;

import com.amalitech.tib.expense.model.Expense;
import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.trip.model.Trip;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
@Table(name = "budget")
public class Budget extends BaseEntity {

    private BigDecimal totalBudget;
    private String currency;
    @Lob
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @OneToMany(mappedBy = "budget")
    private Set<Expense> expenses;
}
