package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.AcademicYear;
import com.school.service.impl.AcademicYearServiceImpl;
import com.school.service.interfaces.AcademicYearService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/academic-years/*")
public class AcademicYearController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final AcademicYearService academicYearService;

    public AcademicYearController() {
        this.academicYearService = new AcademicYearServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/current":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                writeJson(resp, academicYearService.getCurrentAcademicYear());
                return;
            case "/":
            case "":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, academicYearService.getAllAcademicYears());
                return;
            default:
                if (path.startsWith("/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, academicYearService.getAcademicYearById(parseInt(path.substring(1))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        AcademicYear academicYear = readBody(req, AcademicYear.class);
        if (academicYear == null)
            throw new ValidationException("Request body is required.");
        writeJson(resp, academicYearService.createAcademicYear(academicYear), "Academic year created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        String path = pathInfo(req);

        if (path.startsWith("/set-current/")) {
            academicYearService.setCurrentAcademicYear(parseInt(path.substring("/set-current/".length())));
            writeStatusMessage(resp, "Current academic year updated.");
            return;
        }

        int yearId = parseInt(path.substring(1));
        AcademicYear academicYear = readBody(req, AcademicYear.class);
        if (academicYear == null)
            throw new ValidationException("Request body is required.");
        academicYear.setAcademicYearId(yearId);
        academicYearService.updateAcademicYear(academicYear);
        writeStatusMessage(resp, "Academic year updated.");
    }
}
