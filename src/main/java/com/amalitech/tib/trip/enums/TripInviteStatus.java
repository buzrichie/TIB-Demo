package com.amalitech.tib.trip.enums;

/** Represents the status of a trip invitation. */
public enum TripInviteStatus {
  /** The invitation has been sent but not yet responded to. */
  PENDING,
  /** The invitation has been accepted by the user. */
  ACCEPTED,
  /** The invitation has been rejected by the user. */
  REJECTED
}
