package com.jotechhub.organizer;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerDashboardResponse {

    private Long organizerUserId;
    private String organizerName;
    private OrganizerDashboardStatsResponse stats;
    private List<OrganizerDashboardEventItemResponse> recentEvents;
}