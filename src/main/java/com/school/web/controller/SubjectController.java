package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Subject;
import com.school.service.impl.SubjectServiceImpl;
import com.school.service.interfaces.SubjectService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/subjects/*")
public class SubjectController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";

    private final SubjectService subjectService;

    public SubjectController() {
        this.subjectService = new SubjectServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, subjectService.getAllSubjects());
                return;
            }
            case "/count": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, subjectService.getSubjectCount());
                return;
            }
            default:
                if (path.startsWith("/section/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT);
                    String[] parts = path.substring(1).split("/");
                    if (parts.length == 3) {
                        int sectionId = Integer.parseInt(parts[1]);
                        int academicYearId = Integer.parseInt(parts[2]);
                        writeJson(resp, subjectService.getSubjectsBySection(sectionId, academicYearId));
                        return;
                    }
                } else if (path.startsWith("/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT);
                    writeJson(resp, subjectService.getSubjectById(parseInt(path.substring(1))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        Subject subject = readBody(req, Subject.class);
        if (subject == null)
            throw new ValidationException("Request body is required.");
        writeJson(resp, subjectService.createSubject(subject), "Subject created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int subjectId = parseInt(pathInfo(req).substring(1));
        Subject subject = readBody(req, Subject.class);
        if (subject == null)
            throw new ValidationException("Request body is required.");
        subject.setSubjectId(subjectId);
        subjectService.updateSubject(subject);
        writeStatusMessage(resp, "Subject updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int subjectId = parseInt(pathInfo(req).substring(1));
        subjectService.deleteSubject(subjectId);
        writeStatusMessage(resp, "Subject deleted.");
    }
}
