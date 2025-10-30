package com.amalitech.tib.user.model;

import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "account_preferences")
public class AccountPreferences extends BaseEntity {

  private String preferredTravelClass;
  private String mealPreference;
  private String seatPreference;
  private String brandingLogoUrl;
  private String defaultCurrency;
  @Lob private String travelPolicy;
  private String emergencyContactName;
  private String emergencyContactPhone;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private User user;
}
