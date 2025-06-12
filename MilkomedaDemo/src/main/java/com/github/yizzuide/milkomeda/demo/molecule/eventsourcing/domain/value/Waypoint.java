package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.value;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Waypoint(
            String address,
            @JsonProperty("lat")
            double latitude,
            @JsonProperty("lon")
            double longitude
    ) {
    }