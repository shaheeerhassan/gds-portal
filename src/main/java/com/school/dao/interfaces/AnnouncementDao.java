package com.school.dao.interfaces;

import com.school.model.Announcement;
import java.util.List;

public interface AnnouncementDao {
    boolean insertAnnouncement(Announcement announcement);
    List<Announcement> getAllActiveAnnouncements();
    boolean updateAnnouncement(Announcement announcement);
    boolean disableAnnouncement(long announcementId);
    Announcement getAnnouncementById(long announcementId);
    List<Announcement> getAnnouncementsByTargetRole(int roleId);
    List<Announcement> getAnnouncementsByTargetClass(int classId);
    List<Announcement> getAnnouncementsByTargetSection(int sectionId);
}