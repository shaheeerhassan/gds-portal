package com.school.service.impl;

import com.school.dao.impl.AnnouncementDaoImpl;
import com.school.dao.interfaces.AnnouncementDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Announcement;
import com.school.service.interfaces.AnnouncementService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementDao announcementDao;

    public AnnouncementServiceImpl() {
        announcementDao = new AnnouncementDaoImpl();
    }

    @Override
    public Announcement createAnnouncement(Announcement announcement) {
        validateAnnouncement(announcement);

        if (!announcementDao.insertAnnouncement(announcement))
            throw new IllegalStateException("Failed to create announcement.");

        return announcement;
    }

    @Override
    public Announcement getAnnouncementById(long announcementId) {
        validateId(announcementId);
        Announcement announcement = announcementDao.getAnnouncementById(announcementId);
        if (announcement == null)
            throw new ResourceNotFoundException("Announcement not found.");
        return announcement;
    }

    @Override
    public List<Announcement> getAllActiveAnnouncements() {
        return announcementDao.getAllActiveAnnouncements();
    }

    @Override
    public List<Announcement> getAnnouncementsByTargetRole(int roleId) {
        validateId(roleId);
        return announcementDao.getAnnouncementsByTargetRole(roleId);
    }

    @Override
    public List<Announcement> getAnnouncementsByTargetClass(int classId) {
        validateId(classId);
        return announcementDao.getAnnouncementsByTargetClass(classId);
    }

    @Override
    public List<Announcement> getAnnouncementsByTargetSection(int sectionId) {
        validateId(sectionId);
        return announcementDao.getAnnouncementsByTargetSection(sectionId);
    }

    @Override
    public void updateAnnouncement(Announcement announcement) {
        validateId(announcement.getAnnouncementId());

        Announcement existing = announcementDao.getAnnouncementById(announcement.getAnnouncementId());

        if (existing == null)
            throw new ResourceNotFoundException("Announcement not found.");

        if (announcement.getCreatedBy() == 0)
            announcement.setCreatedBy(existing.getCreatedBy());
        if (announcement.getTitle() == null)
            announcement.setTitle(existing.getTitle());
        if (announcement.getContent() == null)
            announcement.setContent(existing.getContent());
        if (announcement.getTargetRoleId() == null)
            announcement.setTargetRoleId(existing.getTargetRoleId());
        if (announcement.getClassId() == null)
            announcement.setClassId(existing.getClassId());
        if (announcement.getSectionId() == null)
            announcement.setSectionId(existing.getSectionId());

        validateAnnouncement(announcement);

        if (!announcementDao.updateAnnouncement(announcement))
            throw new ResourceNotFoundException("Announcement not found.");
    }

    @Override
    public void disableAnnouncement(long announcementId) {
        validateId(announcementId);
        if (!announcementDao.disableAnnouncement(announcementId))
            throw new ResourceNotFoundException("Announcement not found.");
    }

    @Override
    public List<Announcement> getGlobalAnnouncements() {
        return announcementDao.getGlobalAnnouncements();
    }

    private void validateAnnouncement(Announcement announcement) {
        announcement.setTitle(validateRequired(announcement.getTitle(), "Title"));
        announcement.setContent(validateRequired(announcement.getContent(), "Content"));
        validateId(announcement.getCreatedBy());
    }
}
