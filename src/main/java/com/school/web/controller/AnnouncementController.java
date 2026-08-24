package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Announcement;
import com.school.service.impl.AnnouncementServiceImpl;
import com.school.service.interfaces.AnnouncementService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.Registration;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/announcements/*")
public class AnnouncementController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final AnnouncementService announcementService;

    public AnnouncementController() {
        this.announcementService = new AnnouncementServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                writeJson(resp, announcementService.getAllActiveAnnouncements());
                return;
            case "/global":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                writeJson(resp, announcementService.getGlobalAnnouncements());
                return;
            default:
                if (path.startsWith("/id/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                    writeJson(resp, announcementService.getAnnouncementById(parseLong(path.substring("/id/".length()))));
                    return;
                }
                if (path.startsWith("/role/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                    writeJson(resp, announcementService.getAnnouncementsByTargetRole(parseInt(path.substring("/role/".length()))));
                    return;
                }
                if (path.startsWith("/class/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                    writeJson(resp, announcementService.getAnnouncementsByTargetClass(parseInt(path.substring("/class/".length()))));
                    return;
                }
                if (path.startsWith("/section/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                    writeJson(resp, announcementService.getAnnouncementsByTargetSection(parseInt(path.substring("/section/".length()))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);

        Announcement announcement = readBody(req, Announcement.class);
        if (announcement == null)
            throw new ValidationException("Request body is required.");
        if (announcement.getCreatedBy() == 0)
            announcement.setCreatedBy(AuthContext.getUserId(req));
        if (announcement.getCreatedAt() == null)
            announcement.setCreatedAt(java.time.LocalDateTime.now());
        boolean notify = "true".equalsIgnoreCase(req.getParameter("notify"));
        writeJson(resp, announcementService.createAnnouncement(announcement, notify), "Announcement created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        String path = pathInfo(req);

        if (path.startsWith("/disable/")) {
            announcementService.disableAnnouncement(parseLong(path.substring("/disable/".length())));
            writeStatusMessage(resp, "Announcement disabled.");
            return;
        }
        long announcementId = parseLong(path.substring(1));
        Announcement announcement = readBody(req, Announcement.class);
        if (announcement == null)
            throw new ValidationException("Request body is required.");
        announcement.setAnnouncementId(announcementId);
        announcementService.updateAnnouncement(announcement);
        writeStatusMessage(resp, "Announcement updated.");
    }
}
