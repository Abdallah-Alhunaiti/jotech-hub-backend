package com.jotechhub.event;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDeleteEventRequest {

    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    private String cancellationReason;
}
