package com.pavelkostal.aiwithjava.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CapitalWithMoreInfoResponse(
        @JsonPropertyDescription("This is the city name") String capital,
        @JsonPropertyDescription("Population of the city") long population,
        @JsonPropertyDescription("Region of the city or country") String region,
        @JsonPropertyDescription("Primary spoken language in the region") String language,
        @JsonPropertyDescription("Currency used in the region") String currency
) {
}
