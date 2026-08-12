package com.school.service.interfaces;

import com.school.model.Announcement;

import java.util.List;

public interface AnnouncementService {
    Announcement createAnnouncement(Announcement announcement);
    Announcement getAnnouncementById(long announcementId);
    List<Announcement> getAllActiveAnnouncements();
    List<Announcement> getAnnouncementsByTargetRole(int roleId);
    List<Announcement> getAnnouncementsByTargetClass(int classId);
    List<Announcement> getAnnouncementsByTargetSection(int sectionId);
    List<Announcement> getGlobalAnnouncements();
    void updateAnnouncement(Announcement announcement);
    void disableAnnouncement(long announcementId);
}
