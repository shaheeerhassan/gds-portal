package com.school.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BroadcastNotificationRequest {
    private boolean global;
    private List<Integer> targetRoleIds;
    private Notification notification;
}
