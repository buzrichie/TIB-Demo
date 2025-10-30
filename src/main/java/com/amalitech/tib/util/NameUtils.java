package com.amalitech.tib.util;

public class NameUtils {
  /**
   * Splits a full name string into an array of [firstName, lastName].
   *
   * @param fullName The full name string
   * @return A String array where index 0 is firstName and index 1 is lastName.
   */
  public static String[] splitFullName(String fullName) {
    String firstName = null;
    String lastName = null;

    if (fullName == null || fullName.trim().isEmpty()) {
      return new String[] {firstName, lastName};
    }

    String trimmedName = fullName.trim().replaceAll("\\s+", " ");
    int lastSpaceIndex = trimmedName.lastIndexOf(' ');

    if (lastSpaceIndex == -1) {
      firstName = trimmedName;
      lastName = null;
    } else {
      firstName = trimmedName.substring(0, lastSpaceIndex);
      lastName = trimmedName.substring(lastSpaceIndex + 1);
    }

    return new String[] {firstName, lastName};
  }
}
