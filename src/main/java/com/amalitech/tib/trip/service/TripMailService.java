package com.amalitech.tib.trip.service;

/** Service interface for sending trip-related emails. */
public interface TripMailService {

  /**
   * Sends a trip invitation email.
   *
   * @param to The recipient's email address.
   * @param tripTitle The title of the trip.
   * @param link The invitation link.
   * @throws RuntimeException if the email fails to send.
   */
  void sendTripInvite(String to, String tripTitle, String link);
}
