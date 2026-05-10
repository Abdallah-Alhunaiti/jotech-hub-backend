package com.jotechhub.organizer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerDashboardStatsResponse {

    private long totalEvents;
    private long upcomingEvents;
    private long pastEvents;
}