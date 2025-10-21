package com.amalitech.tib.budget.model;

import com.amalitech.tib.shared.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "expense")
public class Expense extends BaseEntity {

    private String item;
    private String payee;
    private BigDecimal price;
    private LocalDate date;
    private Boolean split;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;
}
