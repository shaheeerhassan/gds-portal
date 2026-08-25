package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Section;
import com.school.service.impl.SectionServiceImpl;
import com.school.service.interfaces.SectionService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/sections/*")
public class SectionController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";

    private final SectionService sectionService;

    public SectionController() {
        this.sectionService = new SectionServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, sectionService.getAllSections());
                return;
            }
            case "/count": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, sectionService.getSectionCount());
                return;
            }
            default:
                if (path.startsWith("/class/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    String[] parts = path.substring("/class/".length()).split("/");
                    if (parts.length != 2)
                        throw new ValidationException("Expected /class/{classId}/{academicYearId}.");
                    writeJson(resp, sectionService.getSectionsByClassAndYear(parseInt(parts[0]), parseInt(parts[1])));
                    return;
                }
                if (path.startsWith("/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT);
                    writeJson(resp, sectionService.getSectionById(parseInt(path.substring(1))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        Section section = readBody(req, Section.class);
        if (section == null)
            throw new ValidationException("Request body is required.");
        writeJson(resp, sectionService.createSection(section), "Section created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int sectionId = parseInt(pathInfo(req).substring(1));
        Section section = readBody(req, Section.class);
        if (section == null)
            throw new ValidationException("Request body is required.");
        section.setSectionId(sectionId);
        sectionService.updateSection(section);
        writeStatusMessage(resp, "Section updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int sectionId = parseInt(pathInfo(req).substring(1));
        sectionService.deleteSection(sectionId);
        writeStatusMessage(resp, "Section deleted.");
    }
}
