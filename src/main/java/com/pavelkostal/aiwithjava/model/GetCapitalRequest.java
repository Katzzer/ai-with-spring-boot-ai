package com.pavelkostal.aiwithjava.model;

public record GetCapitalRequest(String stateOrCountry) {

    //Compact constructor for validation
    public GetCapitalRequest {
        // Ensure the field is not null or empty
        if (stateOrCountry == null || stateOrCountry.trim().isEmpty()) {
            throw new IllegalArgumentException("stateOrCountry cannot be null or empty");
        }

        // Enforce a length limit (e.g., maximum of 50 characters)
        if (stateOrCountry.length() > 50) {
            throw new IllegalArgumentException("stateOrCountry cannot exceed 50 characters");
        }

        // Further limitations or validation logic can be added here
        // Example: ensure it only contains alphabetic characters
        if (!stateOrCountry.matches("[a-zA-Z0-9 ]+")) {
            throw new IllegalArgumentException("stateOrCountry can only contain alphabets and spaces");
        }
    }
}
